package de.uni.scrumboard.data;

public interface IProfileRepository {

    Profile getProfile(int accountId);

    boolean saveProfile(Profile profile, int accountId);

}
