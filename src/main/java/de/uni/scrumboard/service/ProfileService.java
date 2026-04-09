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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProfile'");
    }

    @Override
    public boolean saveProfile(Profile profile) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'saveProfile'");
    }
    
}
