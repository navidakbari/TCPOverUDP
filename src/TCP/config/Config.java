package config;

import java.time.Duration;

public class Config {
    public final static int TIMEOUT = (int)Duration.ofMillis(20).toMillis();
}
