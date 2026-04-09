package de.uni.scrumboard.service;

import de.uni.scrumboard.data.Profile;
import de.uni.scrumboard.data.ProfileRepository;

public class ProfileService implements IProfileService {

    private ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public Profile getProfile() {
        return profileRepository.getProfile();
    }

    @Override
    public boolean saveProfile(Profile profile) {
        return profileRepository.saveProfile();
    }
    
}
