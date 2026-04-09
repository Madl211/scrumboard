package de.uni.scrumboard.service;

import de.uni.scrumboard.data.Profile;

public interface IProfileService {

    Profile getProfile();

    boolean saveProfile(Profile profile);

}