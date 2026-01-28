package de.uni.scrumboard;

import java.util.List;

import io.javalin.http.Context;
import static io.javalin.rendering.template.TemplateUtil.model;
import tools.jackson.databind.ObjectMapper;

public class SampleController {

    private SampleService sampleService;
    private ObjectMapper objectMapper;

    public SampleController(SampleService sampleService) {
        this.sampleService = sampleService;
        objectMapper = new ObjectMapper();
    }

    public void getSampleData(Context context) {
        List<SampleData> sampleDataSet = sampleService.getSampleData();
        String sampleDataSetAsJson = objectMapper.writeValueAsString(sampleDataSet);
        context.render("templates/sample_template.mustache",
                model("sampleData.field1", sampleDataSet.get(0).getField1(), "sampleData.field2",
                        sampleDataSet.get(0).getField2()));
    }

}
