package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class JacksonIssueDemo {

  private static class JsonTemplate {
    public final int finalValue = 1234;
  }

  public static void main(String[] args) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    JsonTemplate parsed1 = mapper.readValue("{\"finalValue\": 1234}", JsonTemplate.class);
    JsonTemplate parsed2 = mapper.readValue("{\"finalValue\": 5678}", JsonTemplate.class);
    // Note both of these return 1234 (even if the second JSON input has 5678).
    System.out.println(parsed1.finalValue);
    System.out.println(parsed2.finalValue);
  }
}
