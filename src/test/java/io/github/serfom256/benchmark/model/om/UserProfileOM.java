package io.github.serfom256.benchmark.model.om;

import io.github.serfom256.annotations.entity.Field;
import io.github.serfom256.annotations.entity.Model;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Model(primaryKey = "profileId")
@Getter
@Setter
@NoArgsConstructor
public class UserProfileOM {

    @Field(name = "profile_id")
    private long profileId;

    @Field(name = "user_id")
    private long userId;

    @Field(name = "bio")
    private String bio;

    @Field(name = "avatar_url")
    private String avatarUrl;
}
