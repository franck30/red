package com.benoitquenaudon.tvfoot.red.testutil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.benoitquenaudon.tvfoot.red.app.data.entity.Match;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import javax.inject.Inject;
import timber.log.Timber;

public class Fixture {
  private Gson gson;

  @Inject public Fixture(Gson gson) {
    this.gson = gson;
  }

  private Type listMatchType = new TypeToken<List<Match>>() {
  }.getType();

  public List<Match> anyMatches() {
    return anyObject("matches_sample.json", listMatchType);
  }

  public List<Match> anyMatchesShort() {
    return anyObject("matches_sample_short.json", listMatchType);
  }

  public Match anyMatch() {
    return anyObject("match_sample.json", Match.class);
  }

  public List<Match> anyNextMatches() {
    return anyObject("matches_next_sample.json", listMatchType);
  }

  @SuppressWarnings("TypeParameterUnusedInFormals")
  private <T> T anyObject(String filename, Type typeOfT) {
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filename);

    try {
      JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));

      T object = gson.fromJson(reader, typeOfT);

      reader.close();

      return object;
    } catch (IOException e) {
      Timber.e(e);
      throw new RuntimeException("fixture " + filename + " failed");
    }
  }
}
