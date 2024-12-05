package theproject.config

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

final case class AppConfig(emberConfig: EmberConfig, postgresConfig: PostgresConfig) derives ConfigReader
