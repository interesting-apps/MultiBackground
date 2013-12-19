package com.apps.interestingapps.multibackground.common;

import java.util.ArrayList;
import java.util.List;

public class CommonUtilities {

	/**
	 * Creates a new list that is exact reverse of the input array list
	 *
	 * @param inputList
	 * @return
	 */
	public static <E> List<E> reverseList(ArrayList<E> inputList) {
		if(inputList == null || inputList.size() == 0) {
			return inputList;
		}
		List<E> reversedList = new ArrayList<E>();
		for(int i = inputList.size() - 1 ; i >= 0 ; i--) {
			reversedList.add(inputList.get(i));
		}
		return reversedList;
	}
}
