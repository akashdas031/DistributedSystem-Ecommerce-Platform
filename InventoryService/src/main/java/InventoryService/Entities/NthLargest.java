package InventoryService.Entities;
import java.util.*;
public class NthLargest {

//	public static void main(String[] args) {
//        int[] nums={10,20,15,5,30};
//        System.out.println(getLargest(nums, 2));
//    }
    public static Integer getLargest(int[] nums,int n){
    	Map<Integer,Integer> map=new HashMap<>();
    	for(int i=0;i<nums.length;i++) {
    		map.put(nums[i], i);
    	}
        Arrays.sort(nums);
        int len=nums.length;
        return map.get(nums[len-n]);
    }
}
