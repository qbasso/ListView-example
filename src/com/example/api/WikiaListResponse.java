package com.example.api;

public class WikiaListResponse {
	private WikiModel[] items;
	private int next;
	private int total;
	private int batches;
	private int currentBatch;

	public WikiModel[] getItems() {
		return items;
	}

	public void setItems(WikiModel[] items) {
		this.items = items;
	}

	public int getNext() {
		return next;
	}

	public void setNext(int next) {
		this.next = next;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getBatches() {
		return batches;
	}

	public void setBatches(int batches) {
		this.batches = batches;
	}

	public int getCurrentBatch() {
		return currentBatch;
	}

	public void setCurrentBatch(int currentBatch) {
		this.currentBatch = currentBatch;
	}
}
