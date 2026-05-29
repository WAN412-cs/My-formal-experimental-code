set V;
set E,within V cross V;

var A{i in V,j in V},integer;

minimize shPath: sum{i in V,j in V} A[i,j];

s.t. ji1:A[1,1]+A[5,1]+A[7,1]=11;
s.t. ji2:A[1,2]+A[2,2]+A[6,2]+A[7,2]=23;
s.t. ji3:A[2,3]+A[4,3]+A[6,3]+A[7,3]+A[9,3]=27;
s.t. ji4:A[1,4]+A[2,4]+A[5,4]+A[6,4]+A[7,4]+A[8,4]=33;
s.t. ji5:A[1,5]+A[2,5]+A[4,5]+A[5,5]+A[8,5]+A[9,5]=37;
s.t. ji6:A[3,6]+A[4,6]+A[5,6]+A[8,6]+A[9,6]=27;
s.t. ji7:A[1,7]+A[3,7]+A[4,7]+A[5,7]+A[6,7]+A[8,7]+A[9,7]=41;
s.t. ji8:A[1,8]+A[3,8]+A[4,8]+A[8,8]=23;
s.t. ji9:A[5,9]+A[9,9]=11;
s.t. ji10:A[1,1]+A[1,2]+A[2,2]+A[2,3]=19;
s.t. ji11:A[1,4]+A[1,5]+A[2,4]+A[2,5]+A[3,6]=31;

s.t. fanwei1:A[1,7]+A[1,8]+A[3,7]+A[3,8]=19;
s.t. fanwei2:A[4,3]+A[5,1]+A[6,2]+A[6,3]=20;
s.t. fanwei3:A[4,5]+A[4,6]+A[5,4]+A[5,5]+A[5,6]+A[6,4]=33;
s.t. fanwei4:A[4,7]+A[4,8]+A[5,7]+A[5,9]+A[6,7]=28;

s.t. fanwei5:A[7,1]+A[7,2]+A[7,3]+A[9,3]=22;
s.t. fanwei6:A[7,4]+A[8,4]+A[8,5]+A[8,6]+A[9,5]+A[9,6]=33;

s.t. fanwei7:A[1,1]+A[1,2]+A[1,4]+A[1,5]+A[1,7]+A[1,8]=35;

s.t. fanwei8:A[2,2]+A[2,3]+A[2,4]+A[2,5]=19;
s.t. fanwei9:A[3,6]+A[3,7]+A[3,8]=15;


s.t. fanwei10:A[4,3]+A[4,5]+A[4,6]+A[4,7]+A[4,8]=26;
s.t. fanwei11:A[5,1]+A[5,4]+A[5,5]+A[5,6]+A[5,7]+A[5,9]=33;
s.t. fanwei12:A[6,2]+A[6,3]+A[6,4]+A[6,7]=22;
s.t. fanwei13:A[7,1]+A[7,2]+A[7,3]+A[7,4]=22;
s.t. fanwei14:A[8,4]+A[8,5]+A[8,6]+A[8,7]+A[8,8]=34;
s.t. fanwei15:A[9,3]+A[9,5]+A[9,6]+A[9,7]+A[9,9]=27;




 
s.t. cp1{i in V,j in V}:A[i,j]>=1;
s.t. cp2{i in V,j in V}:A[i,j]<=9;


s.t. c1:sum{j in V}A[1,j]=45;
s.t. c2:sum{j in V}A[2,j]=45;
s.t. c3:sum{j in V}A[3,j]=45;
s.t. c4:sum{j in V}A[4,j]=45;
s.t. c5:sum{j in V}A[5,j]=45;
s.t. c6:sum{j in V}A[6,j]=45;
s.t. c7:sum{j in V}A[7,j]=45;
s.t. c8:sum{j in V}A[8,j]=45;
s.t. c9:sum{j in V}A[9,j]=45;

s.t. cqb101:A[1,3]=3;
s.t. cqb102:A[1,6]=6;
s.t. cq1:A[1,9]=1;
s.t. cqb103:A[2,1]=9;
s.t. cqb104:A[2,6]=1;
s.t. cq2:A[2,7]=3;
s.t. cq3:A[2,8]=7;
s.t. cqb201:A[2,9]=6;
s.t. cqb202:A[3,1]=7;
s.t. cqb203:A[3,2]=1;
s.t. cq4:A[3,3]=6;
s.t. cq5:A[3,4]=3;
s.t. cqb204:A[3,5]=4;
s.t. cqb301:A[3,9]=9;
s.t. cqb302:A[4,1]=1;
s.t. cqb303:A[4,2]=7;
s.t. cqb304:A[4,4]=8;
s.t. cqb305:A[4,9]=3;
s.t. cqb306:A[5,2]=3;
s.t. cqb401:A[5,3]=8;
s.t. cqb402:A[5,8]=1;
s.t. cq6:A[6,1]=6;
s.t. cq7:A[6,5]=1;
s.t. cqb403:A[6,6]=3;
s.t. cqb404:A[6,8]=5;
s.t. cqb501:A[6,9]=8;
s.t. cq8:A[7,5]=3;
s.t. cq9:A[7,6]=8;
s.t. cqb502:A[7,7]=1;
s.t. cqb503:A[7,8]=6;
s.t. cq10:A[7,9]=5;
s.t. cqb601:A[8,1]=3;
s.t. cqb602:A[8,2]=5;
s.t. cqb603:A[8,3]=1;
s.t. cqb604:A[8,9]=2;
s.t. cqb605:A[9,1]=8;
s.t. cqb701:A[9,2]=6;
s.t. cqb7011:A[9,4]=1;
s.t. cqb702:A[9,8]=3;


s.t. c10:sum{j in V}A[j,1]=45;
s.t. c11:sum{j in V}A[j,2]=45;
s.t. c12:sum{j in V}A[j,3]=45;
s.t. c13:sum{j in V}A[j,4]=45;
s.t. c14:sum{j in V}A[j,5]=45;
s.t. c15:sum{j in V}A[j,6]=45;
s.t. c16:sum{j in V}A[j,7]=45;
s.t. c17:sum{j in V}A[j,8]=45;
s.t. c18:sum{j in V}A[j,9]=45;

s.t. cqb7021:A[5,9]>=4;
s.t. cqb7022:A[5,9]<=7;

s.t. cqb7023:A[9,9]>=4;
s.t. cqb7024:A[9,9]<=7;

s.t. cqb7025:A[9,5]>=2;

s.t. cqb7026:A[1,1]>=2;
s.t. cqb7027:A[1,1]<=5;

s.t. cqb7028:A[5,1]>=2;
s.t. cqb7029:A[5,1]<=5;

s.t. cqb7030:A[7,1]>=2;
s.t. cqb7031:A[7,1]<=4;

data;

set V:=1 2 3 4 5 6 7 8 9;    



end;           

			  
			  
			  
			  
