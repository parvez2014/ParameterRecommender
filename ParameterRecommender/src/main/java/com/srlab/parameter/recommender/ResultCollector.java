package com.srlab.parameter.recommender;

public class ResultCollector {

	private int totalTestcases;
	private int recommendationMade;
	private int recommendationMadeUnsuccessful;
	private int recommendationNotMade;
	
	private int top_1;
	private int top_3;
	private int top_5;
	private int top_10;
	
	public ResultCollector() {
		this.totalTestcases = 0;
		this.recommendationMade = 0;
		this.recommendationNotMade = 0;
		this.recommendationMadeUnsuccessful = 0;
		this.top_1 = 0;
		this.top_3 = 0;
		this.top_5 = 0;
		this.top_10 = 0;
	}
	public void add(int rank) {
		this.totalTestcases++;
		if(rank==-1) {
			this.recommendationNotMade++;
		}
		else if(rank==-2) {
			this.recommendationMadeUnsuccessful++;
		}
		else {
			this.recommendationMade++;
			if(rank ==0) {
				top_1++;
			}
			if(rank<3) {
				top_3++;
			}
			if(rank<5) {
				top_5++;
			}
			if(rank<10) {
				top_10++;
			}
		}
	}
	public void print() {
		System.out.println("Total Testcases: "+this.totalTestcases);
		System.out.println("Recommendation Made: "+this.recommendationMade + " Recommendation Nodt Made: "+this.recommendationNotMade+" "+"Recommendation Made Unsuccessful: "+this.recommendationMadeUnsuccessful);
		System.out.println("Top-1: "+top_1+" "+"Top-3: "+top_3+" "+"Top-5: "+top_5+" "+"Top-10: "+top_10);
		System.out.println("PTop-1: "+((top_1*100.0f)/this.totalTestcases)+" "+"Top-3: "+((top_3*100.0f)/this.totalTestcases)+" "+"Top-5: "+((top_5*100.0f)/this.totalTestcases)+" "+"Top-10: "+((top_10*100.0f)/this.totalTestcases));
	}
	
	public int getTotalTestcases() {
		return totalTestcases;
	}
	public int getRecommendationMade() {
		return recommendationMade;
	}
	public int getRecommendationMadeUnsuccessful() {
		return recommendationMadeUnsuccessful;
	}
	public int getRecommendationNotMade() {
		return recommendationNotMade;
	}
	public int getTop_1() {
		return top_1;
	}
	public int getTop_3() {
		return top_3;
	}
	public int getTop_5() {
		return top_5;
	}
	public int getTop_10() {
		return top_10;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}