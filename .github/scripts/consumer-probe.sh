#!/usr/bin/env bash
# Regression guard for the class of bug where a library's published pom pins Spring
# versions from the Boot line it was built against, dragging a stale Spring onto a
# consumer's classpath. Unit tests cannot see this; only dependency resolution can.
#
# Probe A: an application that depends on openMapper ALONE must resolve zero Spring
#          artifacts, because openMapper declares Spring as 'provided'.
# Probe B: a real Boot application must end up with every spring-boot artifact on
#          the Boot line under test, with nothing pinned by openMapper.
set -euo pipefail

BOOT_VERSION="${1:?usage: consumer-probe.sh <spring-boot-version>}"
OM_VERSION="$(mvn -B -q -DforceStdout help:evaluate -Dexpression=project.version)"
PROBE="$(mktemp -d)"
status=0

emit_pom() { # $1=dir  $2=parent-block  $3=extra-deps
  mkdir -p "$1"
  cat > "$1/pom.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    $2
    <groupId>probe</groupId><artifactId>probe</artifactId><version>1.0</version>
    <properties><java.version>17</java.version></properties>
    <dependencies>
        $3
        <dependency>
            <groupId>io.github.serfom256</groupId>
            <artifactId>openmapper-spring-boot-starter</artifactId>
            <version>${OM_VERSION}</version>
        </dependency>
    </dependencies>
</project>
EOF
}

echo "== Probe A: openMapper ${OM_VERSION} alone must contribute no Spring artifact =="
# Every Spring artifact must come from the consuming application. Contributing one
# pins the host's Spring version and breaks any build that enforces dependency
# convergence -- found against Broadleaf Commerce, which spring-jdbc at compile
# scope stopped from even adding the dependency.
emit_pom "$PROBE/a" "" ""
mvn -B -f "$PROBE/a/pom.xml" dependency:tree > "$PROBE/a/tree.txt"
if grep -qE "org\.springframework(\.boot)?:" "$PROBE/a/tree.txt"; then
  echo "FAIL: openMapper pulled in Spring artifacts on its own:"
  grep -E "org\.springframework(\.boot)?:" "$PROBE/a/tree.txt"
  status=1
else
  echo "OK: no Spring artifacts contributed."
fi

echo
echo "== Probe B: inside a Boot ${BOOT_VERSION} application, all Boot artifacts must be ${BOOT_VERSION} =="
emit_pom "$PROBE/b" \
  "<parent><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-parent</artifactId><version>${BOOT_VERSION}</version><relativePath/></parent>" \
  "<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter</artifactId></dependency><dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-jdbc</artifactId></dependency><dependency><groupId>com.h2database</groupId><artifactId>h2</artifactId><scope>runtime</scope></dependency>"
mvn -B -f "$PROBE/b/pom.xml" dependency:tree > "$PROBE/b/tree.txt"
offending="$(grep -oE "org\.springframework\.boot:[A-Za-z0-9._-]+:jar:[^:]+" "$PROBE/b/tree.txt" \
             | grep -v ":jar:${BOOT_VERSION}\$" || true)"
if [ -n "$offending" ]; then
  echo "FAIL: spring-boot artifacts off the ${BOOT_VERSION} line:"
  echo "$offending"
  status=1
else
  echo "OK: every spring-boot artifact is on the ${BOOT_VERSION} line."
fi

# Spring Framework modules ship in lockstep, so more than one version among them
# means something pinned its own -- the shape of the bug this probe exists for.
framework_versions="$(grep -oE "org\.springframework:[A-Za-z0-9._-]+:jar:[^:]+" "$PROBE/b/tree.txt" \
                      | sed 's/.*:jar://' | sort -u)"
if [ "$(echo "$framework_versions" | wc -l)" -gt 1 ]; then
  echo "FAIL: mixed Spring Framework versions on one classpath:"
  echo "$framework_versions"
  status=1
else
  echo "OK: Spring Framework modules agree on ${framework_versions}."
fi

echo
echo "== Probe C: a Boot ${BOOT_VERSION} application must actually start =="
mkdir -p "$PROBE/b/src/main/java/probe"
cat > "$PROBE/b/src/main/java/probe/ProbeApp.java" <<'JAVA'
package probe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ProbeApp {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ProbeApp.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            System.out.println("PROBE_CONTEXT_STARTED");
        }
    }
}
JAVA
if mvn -B -f "$PROBE/b/pom.xml" -q compile exec:java \
      -Dexec.mainClass=probe.ProbeApp > "$PROBE/b/run.txt" 2>&1 \
   && grep -q PROBE_CONTEXT_STARTED "$PROBE/b/run.txt"; then
  echo "OK: context started against an embedded DataSource."
else
  echo "FAIL: context did not start. A required dependency is probably 'provided':"
  grep -E "NoClassDefFoundError|ClassNotFoundException|Caused by|APPLICATION FAILED|Description:" -A2 \
       "$PROBE/b/run.txt" | head -12
  status=1
fi

echo
echo "== The starter must ship no application class of its own =="
if unzip -l ~/.m2/repository/io/github/serfom256/openmapper-spring-boot-starter/"${OM_VERSION}"/openmapper-spring-boot-starter-"${OM_VERSION}".jar \
     | grep -q "OpenMapperApplication"; then
  echo "FAIL: OpenMapperApplication is packaged in the published jar"
  status=1
else
  echo "OK: no application class packaged."
fi

exit $status
