package theproject.config

import com.comcast.ip4s.{Host, Port}
import pureconfig.ConfigReader
import pureconfig.error.CannotConvert
import pureconfig.generic.derivation.default.*


final case class Config(host: Host, port: Port) derives ConfigReader

object Config:
  given ConfigReader[Host] = ConfigReader[String].emap: hostString =>
    Host
      .fromString(hostString)
      .toRight(CannotConvert(hostString, Host.getClass.toString, s"Invalid host string: $hostString"))

  given ConfigReader[Port] = ConfigReader[Int].emap: portInt =>
    Port
      .fromInt(portInt)
      .toRight(CannotConvert(portInt.toString, Port.getClass.toString, s"Invalid port number: $portInt"))
