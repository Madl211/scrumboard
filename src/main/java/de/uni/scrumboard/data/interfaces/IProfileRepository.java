package de.uni.scrumboard.data.interfaces;

import de.uni.scrumboard.data.Profile;

public interface IProfileRepository {

    Profile getProfile(int accountId);

    boolean saveProfile(Profile profile, int accountId);

}
