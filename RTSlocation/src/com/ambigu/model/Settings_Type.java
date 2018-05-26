package com.ambigu.model;

public enum Settings_Type {
	UPDATE_TIMES(0), UPDATE_INVITIMES(1),AUTH_ALLOW(2){
				@Override
				public boolean isRest() {
					return true;
				}
			};

	private int value;

	private Settings_Type(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public boolean isRest() {
		return false;
	}
}
