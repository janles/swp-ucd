package com.example.neuegruppeerstellen;

public class MessageDetails {
	int icon;
	String name;
	String eta;
	String speed;
	String averageSpeed;
	String distToGoal;
	String distToMe;

	public String getSpeed() {
		return speed;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}

	public String getAverageSpeed() {
		return averageSpeed;
	}

	public void setAverageSpeed(String averageSpeed) {
		this.averageSpeed = averageSpeed;
	}

	public String getDistToGoal() {
		return distToGoal;
	}

	public void setDistToGoal(String distToGoal) {
		this.distToGoal = distToGoal;
	}

	public String getDistToMe() {
		return distToMe;
	}

	public void setDistToMe(String distToMe) {
		this.distToMe = distToMe;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEta() {
		return eta;
	}

	public void setEta(String eta) {
		this.eta = eta;
	}

	public String toString() {
		return name + " - " + eta;
	}

}
