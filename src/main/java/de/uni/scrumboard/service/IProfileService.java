package de.uni.scrumboard.service;

import de.uni.scrumboard.data.Profile;

public interface IProfileService {

    Profile getProfile(int accountId);

    boolean saveProfile(Profile profile, int accountId);

}