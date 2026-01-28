package de.uni.scrumboard;

import java.util.List;

public class SampleService {

    private SampleDataRepository sampleDataRepository;

    public SampleService(SampleDataRepository sampleDataRepository) {
        this.sampleDataRepository = sampleDataRepository;
    }

    public List<SampleData> getSampleData() {
        return sampleDataRepository.getSampleData();
    }

}
