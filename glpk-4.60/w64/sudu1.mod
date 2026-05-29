set V;
set E1,within V cross V;
set E2,within V cross V;
set E3,within V cross V;
set E4,within V cross V;
set E5,within V cross V;
set E6,within V cross V;
set E7,within V cross V;
set E8,within V cross V;
set E9,within V cross V;

var x{i in V,j in V},integer;
var n{i in V,j in V},integer;

/*minimize shPath: sum{(i,j) in E1} x[i,j]+sum{(i,j) in E2} x[i,j]+sum{(i,j) in E3} x[i,j]+sum{(i,j) in E4} x[i,j]+sum{(i,j) in E5} x[i,j]+sum{(i,j) in E6} x[i,j]+sum{(i,j) in E7} x[i,j]+sum{(i,j) in E8} x[i,j]+sum{(i,j) in E9} x[i,j];*/
minimize shPath: sum{i in V,j in V} x[i,j];

/*s.t. fen1:x[1,2]+1<=x[1,4];*/
s.t. fen1:x[4,4]=1;
s.t. fen2:x[4,9]=9;
s.t. fen3:x[7,3]=3;
s.t. fen4:x[7,7]=6;
s.t. fen5:x[7,1]=5;
s.t. fen6:x[8,6]=2;
s.t. fen7:x[9,3]=8;
s.t. fen8:x[8,5]=3;
s.t. fen9:x[5,7]+1<=x[5,8];
s.t. fen10:x[5,1]+1<=x[5,5];
s.t. fen11:x[6,1]+1<=x[6,9];
s.t. fen12:x[6,2]+1<=x[6,6];
s.t. fen13:x[6,7]+1<=x[6,9];
s.t. fen14:x[6,7]+1<=x[6,8];
s.t. fen15:x[6,3]+1<=x[6,9];
s.t. fen16:x[6,2]+1<=x[6,4];
s.t. fen17:x[6,6]+1<=x[6,1];
s.t. fen18:x[9,1]+1<=x[9,4];
s.t. fen19:x[9,5]+1<=x[9,3];
s.t. fen20:x[2,3]+1<=x[2,4];
s.t. fen21:x[2,4]+1<=x[2,2];
s.t. fen22:x[2,7]+1<=x[2,9];
s.t. fen23:x[1,6]+1<=x[1,9];
s.t. fen24:x[1,8]+1<=x[1,1];
s.t. fen25:x[1,2]+1<=x[1,9];
s.t. fen26:x[1,2]+1<=x[1,1];
s.t. fen27:x[1,1]+1<=x[1,9];
s.t. fen28:x[1,4]+1<=x[1,1];
s.t. fen29:x[1,2]+1<=x[1,4];
s.t. fen30:x[1,6]+1<=x[1,1];








/*s.t. ji1:x[1,9]=3*n[1,9];*/
s.t. ji2:x[2,1]=3*n[2,1];
/*s.t. ji3:x[2,4]=3*n[2,4];*/
/*s.t. ji4:x[2,6]=3*n[2,6];*/
/*s.t. ji5:x[2,7]=3*n[2,7];*/
s.t. ji6:x[3,1]=3*n[3,1];
/*s.t. ji7:x[3,9]=3*n[3,9];*/
s.t. ji8:x[4,4]=2*n[4,4]+1;
s.t. ji9:x[4,9]=2*n[4,9]+1;
s.t. ji10:x[6,9]=2*n[6,9]+1;
s.t. ji11:x[7,1]=2*n[7,1]+1;
s.t. ji12:x[7,7]=3*n[7,7];
s.t. ji13:x[7,8]=3*n[7,8];
s.t. ji14:x[8,6]=2*n[8,6];
s.t. ji15:x[9,8]=3*n[9,8];

s.t. fanwei1:x[1,1]<=9;
s.t. fanwei2:x[1,1]>=3;
s.t. fanwei3:x[1,2]<=9;
s.t. fanwei4:x[1,2]>=3;
s.t. fanwei5:x[1,4]<=9;
s.t. fanwei6:x[1,4]>=2;
s.t. fanwei7:x[1,7]<=9;
s.t. fanwei8:x[1,7]>=2;
s.t. fanwei9:x[2,1]<=9;
s.t. fanwei10:x[2,1]>=3;
s.t. fanwei11:x[3,1]>=3;
s.t. fanwei12:x[3,5]>=2;
s.t. fanwei13:x[3,8]>=2;
s.t. fanwei14:x[5,2]>=2;
s.t. fanwei15:x[5,5]>=2;
s.t. fanwei16:x[5,8]>=2;
s.t. fanwei17:x[6,2]>=3;
s.t. fanwei18:x[6,8]>=2;
s.t. fanwei19:x[7,1]<=7;
s.t. fanwei20:x[7,1]>=5;
s.t. fanwei21:x[7,2]<=8;
s.t. fanwei22:x[7,2]>=2;
s.t. fanwei23:x[7,3]<=8;
s.t. fanwei24:x[7,3]>=3;
s.t. fanwei25:x[7,4]>=2;
s.t. fanwei26:x[7,6]>=2;
s.t. fanwei27:x[7,7]>=6;
s.t. fanwei28:x[7,8]>=3;
s.t. fanwei29:x[8,2]<=6;
s.t. fanwei30:x[8,2]>=2;
s.t. fanwei31:x[8,5]<=3;
s.t. fanwei32:x[8,5]>=2;
s.t. fanwei33:x[8,6]<=6;
s.t. fanwei34:x[8,6]>=2;
s.t. fanwei35:x[9,1]<=8;
s.t. fanwei36:x[9,1]>=3;
s.t. fanwei37:x[9,3]<=8;
s.t. fanwei38:x[9,3]>=3;
s.t. fanwei39:x[9,4]>=3;
s.t. fanwei40:x[9,5]>=3;
s.t. fanwei41:x[9,8]>=3;



 
s.t. cp1{i in V,j in V}:x[i,j]>=1;
s.t. cp2{i in V,j in V}:x[i,j]<=9;


s.t. c1:sum{j in V}x[1,j]=45;
s.t. c2:sum{j in V}x[2,j]=45;
s.t. c3:sum{j in V}x[3,j]=45;
s.t. c4:sum{j in V}x[4,j]=45;
s.t. c5:sum{j in V}x[5,j]=45;
s.t. c6:sum{j in V}x[6,j]=45;
s.t. c7:sum{j in V}x[7,j]=45;
s.t. c8:sum{j in V}x[8,j]=45;
s.t. c9:sum{j in V}x[9,j]=45;

s.t. cqb101:x[1,3]=1;
s.t. cqb102:x[1,5]=4;
s.t. cq1:x[1,6]=7;
s.t. cqb103:x[1,8]=5;
s.t. cqb104:x[2,2]=4;
s.t. cq2:x[2,3]=2;
s.t. cq3:x[2,5]=5;
s.t. cqb201:x[2,8]=7;
s.t. cqb202:x[2,9]=8;
s.t. cqb203:x[3,2]=5;
s.t. cq4:x[3,3]=7;
s.t. cq5:x[3,4]=8;
s.t. cqb204:x[3,7]=4;
s.t. cqb301:x[4,1]=2;
s.t. cqb302:x[4,3]=6;
s.t. cqb304:x[4,5]=7;
s.t. cqb305:x[4,6]=5;
s.t. cqb306:x[4,7]=3;
s.t. cqb401:x[4,8]=4;
s.t. cqb402:x[5,3]=4;
s.t. cq6:x[5,6]=3;
s.t. cq7:x[5,7]=5;
s.t. cqb403:x[6,3]=5;
s.t. cqb404:x[6,4]=4;
s.t. cqb501:x[6,5]=6;
s.t. cq8:x[7,5]=1;
s.t. cq9:x[7,9]=4;
s.t. cqb502:x[8,1]=4;
s.t. cqb503:x[8,3]=9;
s.t. cq10:x[8,4]=7;
s.t. cqb601:x[8,7]=8;
s.t. cqb602:x[8,8]=1;
s.t. cqb603:x[8,9]=5;
s.t. cqb604:x[9,2]=1;
s.t. cqb605:x[9,6]=4;
s.t. cqb701:x[9,7]=7;
s.t. cqb702:x[9,9]=2;

s.t. c10:sum{j in V}x[j,1]=45;
s.t. c11:sum{j in V}x[j,2]=45;
s.t. c12:sum{j in V}x[j,3]=45;
s.t. c13:sum{j in V}x[j,4]=45;
s.t. c14:sum{j in V}x[j,5]=45;
s.t. c15:sum{j in V}x[j,6]=45;
s.t. c16:sum{j in V}x[j,7]=45;
s.t. c17:sum{j in V}x[j,8]=45;
s.t. c18:sum{j in V}x[j,9]=45;



s.t. c20:sum{(i,j) in E1}x[i,j]=45;
s.t. c21:sum{(i,j) in E2}x[i,j]=45;
s.t. c22:sum{(i,j) in E3}x[i,j]=45;
s.t. c23:sum{(i,j) in E4}x[i,j]=45;
s.t. c24:sum{(i,j) in E5}x[i,j]=45;
s.t. c25:sum{(i,j) in E6}x[i,j]=45;
s.t. c26:sum{(i,j) in E7}x[i,j]=45;
s.t. c27:sum{(i,j) in E8}x[i,j]=45;
s.t. c28:sum{(i,j) in E9}x[i,j]=45;



data;

set V:=1 2 3 4 5 6 7 8 9;      
set E1:=
1 1
1 2
1 3
2 1
2 2
2 3
3 1
3 2
3 3
;
set E2:=
1 4
1 5
1 6
2 4
2 5
2 6
3 4
3 5
3 6
;
set E3:=
1 7
1 8
1 9
2 7
2 8
2 9
3 7
3 8
3 9
;
set E4:=
4 1
4 2
4 3
5 1
5 2
5 3
6 1
6 2
6 3
;
set E5:=
4 4
4 5
4 6
5 4
5 5
5 6
6 4
6 5
6 6
;
set E6:=
4 7
4 8
4 9
5 7
5 8
5 9
6 7
6 8
6 9
;
set E7:=
7 1
7 2
7 3
8 1
8 2
8 3
9 1
9 2
9 3
;
set E8:=
7 4
7 5
7 6
8 4
8 5
8 6
9 4
9 5
9 6
;
set E9:=
7 7
7 8
7 9
8 7
8 8
8 9
9 7
9 8
9 9
;
end;           

			  
			  
			  
			  
