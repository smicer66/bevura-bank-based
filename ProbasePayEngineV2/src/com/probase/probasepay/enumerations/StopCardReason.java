package com.probase.probasepay.enumerations;

public enum StopCardReason {
	
	CARD_LOST(1), CARD_STOLEN(2), PENDING_QUERY(3), CARD_CONSOLIDATION(4), CARD_INACTIVE(5), PIN_TRIES_EXCEEDED(6), SUSPECTED_FRAUD(7), CARD_REPLACED(8);
	
	private int value;

	private StopCardReason(int value) {
	    this.value = value;
	}
	
	public int getValue() {
	    return value;
	}
	
	
	public static StopCardReason getStopCardReasonByIntValue(int val)
	{
		for (StopCardReason l : StopCardReason.values()) {
	          if (l.value == val) return l;
		}
		throw new IllegalArgumentException("Leg not found. Amputated?");
	}
}
