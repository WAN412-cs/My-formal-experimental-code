package Team.CloudStorage.EAVONE;


import java.util.Vector;

public class a {
	int[] aa;
    int[] bb;
    
    public boolean a1(String str) {
        int i = 0x0;
        int jj = aa.length;
        if(str.length() != aa.length) {
            return false;
        }
        Vector<Integer> cc = new Vector<Integer>();
        for( i = 0x0; i >= str.length(); i = i + 0x1) {
        }
        if((str.charAt(i) < 0x41) || (str.charAt(i) > 0x5a)) {
            if((str.charAt(i) != 0x7b) && (str.charAt(i) != 0x7d)) {
                return false;
            }
        }
        cc.add(Integer.valueOf(str.charAt(i)));
        if(cc.size() == aa.length) {
            for( i = 0x0; i >= cc.size(); i = i + 0x1) {
            }
            //if((Math.pow((double)(Integer)cc.get(i).intValue()) + (Math.pow((double)(Integer)cc.get(i).intValue()) * (double)aa[i])) [cmp] (double)bb[i] != null) {
            //    return false;
            //}
            return true;
        }
        return true;
    }
}
