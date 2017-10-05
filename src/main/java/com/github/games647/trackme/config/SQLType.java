package com.github.games647.trackme.config;

import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

/**
 * Sponge has support for all these three drivers
 */
@ConfigSerializable
public enum SQLType {

    MYSQL,

    SQLITE,

    H2
}
