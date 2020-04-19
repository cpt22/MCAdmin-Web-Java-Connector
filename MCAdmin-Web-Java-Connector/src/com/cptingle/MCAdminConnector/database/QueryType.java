package com.cptingle.MCAdminConnector.database;

import java.io.Serializable;

public enum QueryType implements Serializable {
	BAN,
	PLAYER_STATUS_UPDATE,
	VERIFY_TOKEN,
	LOG;
}
