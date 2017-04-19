import java.util.*;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by 罗炜尧 on 2017/4/18.
 */
public class BooleanMin {
    private List<String> primeImplicants = new ArrayList<String>();
    private Set<String> remove = new HashSet<String>();
    private List<ArrayList<Integer>> Table = new ArrayList<ArrayList<Integer>>();
    private List<String> FinalResult = new ArrayList<>();
    private HashMap<String,String> reducedPrimGrid = new HashMap<>();

    public String runBooleanMin(String[] lists){
        List<String> a = Arrays.asList(lists);
        genertTable(a);
        String s = null;
        try {
            s=getResult(geneExpression());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return FinalResult(s);

    }

    private String FinalResult(String expression){
        List<String> list = Arrays.asList(expression.split("\\+"));
        String min = null;
        int x=Integer.MAX_VALUE;
        for(String s:list){
            if(s.length()<x){
                min=s;
                x=s.length();
            }
        }
        List<String> list2 = Arrays.asList(min.split("\\*"));

        list2.forEach((String s)->FinalResult.add(reducedPrimGrid.get(s)));
        StringBuffer result = new StringBuffer();
        FinalResult.forEach((String s)->{
            for(int i=0;i<s.length();i++){
                if(s.charAt(i)=='_'){
                    continue;
                }else if(s.charAt(i)=='1'){
                    result.append((char)('A'+i));
                }else if(s.charAt(i)=='0'){
                    result.append((char)('a'+i));
                }
            }
            result.append("+");
        });

        return result.deleteCharAt(result.length()-1).toString();
    }

    private String getResult(String s) throws Exception {
        int braces = s.length()-s.replaceAll("\\)","").length();
        for(int i=0;i<braces-1;i++){
            String sub1 = s.substring(s.indexOf('(')+1,s.indexOf(')'));
            s = s.substring(s.indexOf(')')+1);
            String sub2 = s.substring(s.indexOf('(')+1,s.indexOf(')'));
            s = s.substring(s.indexOf(')')+1);

            s = mutipleTwoTerm(sub1,sub2)+s;
        }

        return  removeBraces(s);
    }

    private String mutipleTwoTerm(String s1,String s2){

        String[] X1 = s1.split("\\+");
        String[] X2 = s2.split("\\+");

        StringBuffer temp1 = new StringBuffer();
        for(int i=0;i<X1.length;i++){
            for(int j=0;j<X2.length;j++){
                temp1.append(mutiplyRule(X1[i],X2[j])+"+");
            }
        }

        String temp2 = plusRule(temp1.deleteCharAt(temp1.length()-1).toString());
        String result = mergRule(temp2);

        return result;
    }

    private String removeBraces(String s) throws Exception {
        if(!s.contains("(")||!s.contains(")")){
            throw new Exception();
        }
        StringBuffer result =new StringBuffer(s);
        if(s.contains("("))
            result.deleteCharAt(0);
        if (s.contains(")"))
            result.deleteCharAt(result.length()-1);
        return result.toString();
    }

    private String mutiplyRule(String s1,String s2){
        List<String> S1 = Arrays.asList(s1.split("\\*"));
        List<String> S2 = Arrays.asList(s2.split("\\*"));
        StringBuffer result = new StringBuffer();
        Set<String> set = new HashSet<>();
        set.addAll(S1);
        set.addAll(S2);
        for(String s:set){
            result.append(s+"*");
        }
        return result.deleteCharAt(result.length()-1).toString();
    }

    private String plusRule(String s1){
        String[] S1 = s1.split("\\+");
        StringBuffer result = new StringBuffer();
        Set<Set<String>> set = new HashSet<>();
        for(int i = 0;i<S1.length;i++){
            List<String> l =  Arrays.asList(S1[i].split("\\*"));
            Set<String> tempSet = new HashSet<>();
            tempSet.addAll(l);
            set.add(tempSet);
        }
        for(Set<String> s:set){
            for(String str:s){
                result.append(str+"*");
            }
            result.deleteCharAt(result.length()-1).append("+");
        }
        return result.deleteCharAt(result.length()-1).toString();
    }

    private String mergRule(String s1){
        StringBuffer result = new StringBuffer();
        result.append("(");
        String[] S1 = s1.split("\\+");
        List<List<String>> strings = new ArrayList<>();
        for(int i = 0;i<S1.length;i++){
            List<String> l =  Arrays.asList(S1[i].split("\\*"));
            strings.add(l);
        }
        List<List<String>> tempList = new ArrayList<>();
        Set<List<String>> set = new HashSet<>();
        Set<List<String>> reduces = new HashSet<>();
        for(List<String> a:strings){
            tempList.clear();
            tempList.addAll(strings);
            tempList.remove(a);

            for(List<String> temp:tempList){
                if(a.containsAll(temp)&&!temp.containsAll(a)){
                    set.add(temp);
                    reduces.add(a);
                }else if (!a.containsAll(temp)&&temp.containsAll(a)){
                    set.add(a);
                    reduces.add(temp);
                }else {
                    set.add(a);
                    set.add(temp);
                }
            }
        }
        set.removeAll(reduces);
        for(List<String> s:set){
            StringBuffer temp = new StringBuffer();
            for(String sa:s){
                temp.append(sa+"*");
            }
            temp.deleteCharAt(temp.length()-1);

            result.append(temp+"+");
        }
        result.deleteCharAt(result.length()-1).append(")");
        return result.toString();
    }


    private String geneExpression() throws Exception {
        if(Table.size()==0){
            throw new Exception();
        }
        int rows = Table.size();
        int colonms = Table.get(0).size();

        HashMap<Integer,Integer> map = new HashMap<>();

        for(int i=0;i<colonms;i++){
            int countOf1 = 0;
            int LastRowOf1 = 0;
            for(int j=0;j<rows;j++){
                if(Table.get(j).get(i)==1){
                    countOf1++;
                    LastRowOf1 = j;
                }
            }
            if(countOf1==1){
                map.put(LastRowOf1,i);
            }
        }

        for(Map.Entry<Integer,Integer> entry:map.entrySet()){
            FinalResult.add(primeImplicants.get(entry.getKey()));
            for(ArrayList<Integer> arrayList:Table){
                if(arrayList.size()==0)
                    continue;
                arrayList.remove((int)entry.getValue());
            }
            Table.set(entry.getKey(),new ArrayList<Integer>());
            primeImplicants.set(entry.getKey(),"DELETE");
        }

        Iterator<ArrayList<Integer>> iterator = Table.iterator();
        while(iterator.hasNext()){
            ArrayList<Integer> arrayList = iterator.next();
            if(arrayList.size() == 0)
                iterator.remove();
        }

        while(primeImplicants.contains("DELETE")){
            primeImplicants.remove("DELETE");
        }

        int count = 0;
        for(String s:primeImplicants){
            reducedPrimGrid.put("P"+count,s);
            count++;
        }

        StringBuffer LogicExpression = new StringBuffer();

        remov0Colounm();

        rows = Table.size();
        colonms = Table.get(0).size();
        for(int i=0;i<colonms;i++){
            LogicExpression.append("(");
            for(int j=0;j<rows;j++){
                if(Table.get(j).get(i)==1){
                    LogicExpression.append("P"+j+"+");
                }
            }
            LogicExpression.deleteCharAt(LogicExpression.length()-1).append(")");
        }
        return LogicExpression.toString();
    }

    private void remov0Colounm() throws Exception {
        if(Table.size()==0){
            throw new Exception();
        }
        int rows = Table.size();
        int colonms = Table.get(0).size();


        for(int i=0;i<colonms;i++){
            int countOf1 = 0;
            for(int j=0;j<rows;j++){
                if(Table.get(j).get(i)==1){
                    countOf1++;
                }
            }
            if(countOf1==0){
                for(int j=0;j<rows;j++){
                        Table.get(j).remove(i);
                    }
            }
        }
    }
    private void genertTable(List<String> list){
        gengentorPIs(list);

        int rows = primeImplicants.size();
        int colunm = list.size();
        for(int i=0;i<rows;i++){
            Table.add(new ArrayList<Integer>());
        }
        for(ArrayList<Integer> arrayList:Table){
            for(int i=0;i<colunm;i++){
                arrayList.add(0);
            }
        }

        int count = 0;
        for(String s:primeImplicants){
            if(s.contains("_")){
                List<String> temp = getNums(s);
                for(int i=0;i<temp.size();i++){
                    Table.get(count).set(list.indexOf(temp.get(i)),1);
                }
            }else {
                Table.get(count).set(list.indexOf(s),1);
            }
            count++;
        }
    }

    private List<String> getNums(String s){
        List<String> result = new ArrayList<>();
        if(!s.contains("_")){
            result.add(s);
            return result;
        }
        int i = s.indexOf("_");
        char[] temp = s.toCharArray();
        temp[i] = '1';
        String s1 = new String(temp);
        temp[i] = '0';
        String s2 = new String(temp);

        List l1 = getNums(s1);
        List l2 = getNums(s2);

        result.addAll(l1);
        result.addAll(l2);

        return result;
    }

    private void gengentorPIs(List<String> list){
        while(true){
            List<String>[] lists = classfied(list);
            list=implicantLoop(lists);
            if(list.size()==0){
                break;
            }
        }
        for(String s:remove){
            if(primeImplicants.contains(s)){
                primeImplicants.remove(s);
            }
        }
        HashSet h = new HashSet(primeImplicants);
        primeImplicants.clear();
        primeImplicants.addAll(h);
    }

    private List<String>[] classfied(List<String> arrayList){
        Set<Integer> set = new HashSet<Integer>();
        for(String s:arrayList){
            set.add(s.length()-s.replaceAll("1","").length());
        }
        int size = set.size();
        List<String>[] lists = new ArrayList[size];
        for(int i=0;i<size;i++){
            lists[i] = new ArrayList<>();
        }
        int init = 0;
        for(Integer i:set){
            for(String s:arrayList){
                if(s.length()-s.replaceAll("1","").length()==i){
                    lists[init].add(s);
                }
            }
            init++;
        }
        return lists;
    }

    private List<String> implicantLoop(List<String>[] lists){
        List<String> result = new ArrayList<String>();
        if(lists.length==1){
            for(String s:lists[0]){
                primeImplicants.add(s);
            }
            return result;
        }

        for(int i=0;i+1<lists.length;i++){
            for(String s:lists[i]){
                int counts = 0;
                for(String ss:lists[i+1]){
                    int mi = checkDiff(s,ss);
                    if(mi==-1){
                        counts++;
                        continue;
                    }else {
                        char[] temp = s.toCharArray();
                        temp[mi] = '_';
                        String tempp = new String(temp);
                        result.add(tempp);
                        remove.add(s);
                        remove.add(ss);
                    }
                }
                if(counts==lists[i+1].size()){
                    primeImplicants.add(s);
                }
            }
        }
        for(String s:lists[lists.length-1]){
            primeImplicants.add(s);
        }
        return result;
    }

    private int checkDiff(String s1,String s2){
        int difPoint = 0;
        int diffCount = 0;
        for(int i=0;i<s1.length();i++){
            if(s1.charAt(i)!=s2.charAt(i)){
                difPoint = i;
                diffCount++;
            }
        }
        if(diffCount!=1){
            return -1;
        }
        return difPoint;
    }
}
