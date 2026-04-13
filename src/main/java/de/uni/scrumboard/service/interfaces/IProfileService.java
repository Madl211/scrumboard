package de.uni.scrumboard.service.interfaces;

import de.uni.scrumboard.data.Profile;

public interface IProfileService {

    Profile getProfile(int accountId);

    boolean saveProfile(Profile profile, int accountId);

}