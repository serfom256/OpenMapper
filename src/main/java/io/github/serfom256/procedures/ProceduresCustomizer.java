package io.github.serfom256.procedures;

/**
 * Changes how {@link Procedures} is built. Declare one as a bean and openMapper applies
 * it before creating the instance; several are applied in {@code @Order}.
 *
 * <pre>{@code
 * @Component
 * public class ReportProceduresCustomizer implements ProceduresCustomizer {
 *
 *     public void customize(ProceduresBuilder builder) {
 *         builder.fetchSize(500).queryTimeoutSeconds(30);
 *     }
 * }
 * }</pre>
 */
@FunctionalInterface
public interface ProceduresCustomizer {

    /**
     * @param builder the builder openMapper is about to call {@code build()} on
     */
    void customize(ProceduresBuilder builder);
}
