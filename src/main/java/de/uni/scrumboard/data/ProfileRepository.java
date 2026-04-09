package de.uni.scrumboard.data;

public class ProfileRepository implements IProfileRepository {

    @Override
    public Profile getProfile() {
        return new Profile("Test", "test@test.de", "0815", "18059", "Rostock", "Albert-Einstein-Straße", "1a");
    }

    @Override
    public boolean saveProfile() {
        return true;
    }

}  
