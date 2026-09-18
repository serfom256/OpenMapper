package io.github.serfom256.benchmark.model.om;

import io.github.serfom256.annotations.entity.Field;
import io.github.serfom256.annotations.entity.Joined;
import io.github.serfom256.annotations.entity.Model;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Model(primaryKey = "id")
@Getter
@Setter
@NoArgsConstructor
public class UserOM {

    @Field(name = "id")
    private long id;

    @Field(name = "username")
    private String username;

    @Field(name = "email")
    private String email;

    @Joined(joinBy = "id", to = "userId")
    private UserProfileOM profile;
}
