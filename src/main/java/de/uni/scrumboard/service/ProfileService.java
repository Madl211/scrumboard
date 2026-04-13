package de.uni.scrumboard.service;

import de.uni.scrumboard.data.Profile;
import de.uni.scrumboard.data.ProfileRepository;
import de.uni.scrumboard.service.interfaces.IProfileService;

public record ProfileService(ProfileRepository profileRepository) implements IProfileService {

    @Override
    public Profile getProfile(int accountId) {
        return profileRepository.getProfile(accountId);
    }

    @Override
    public boolean saveProfile(Profile profile, int accountId) {
        return profileRepository.saveProfile(profile, accountId);
    }

}
