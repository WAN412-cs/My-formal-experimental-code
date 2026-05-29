set V;
set E, within V cross V;
check{(i,j) in E}: i != j;
set S;
set T;
set NoST;
set BSet;
set MSet; 
set MBSet; 
set BMatch{i in MBSet};
/*set AlloSlotsSet; */
/*set PSet; PSet={0,1,...,M-3}*/
set PMSet{i in MBSet};
set I{i in V};
set O{i in V};

param e{(i,j) in E,k in MSet}, binary;
param B, integer, >=0;
param M, integer, >=0;

var x{(i,j) in E}, binary;
/*Decide if the link (i,j) is selected to the shortest path. */

var f{(i,j) in E,k in MSet}, binary;
/*Decide if the slot k of the link (i,j) is selected to the shortest path. */

/*var y{(i,j) in E,k in BSet}, binary;*/

/*var zx{(i,j) in E}, integer;*/

/*var c, integer;*/



/*var z{(i,j) in E,(j,p) in E}, binary; */
/*Decide if the links (i,j) and (j,p) are selected to the shortest path. */

/*minimize shPath: sum{(i,j) in E} x[i,j];*/
/*minimize shPath: c+sum{(i,j) in E} x[i,j];*/
minimize shPath: sum{(i,j) in E} x[i,j];

/* The shortest path, that is the single path from s to t (7).*/

s.t. SinglePath1{i in S}: (sum{(i,j) in E} x[i,j] - sum{(j,i) in E} x[j,i])=1;
s.t. SinglePath2{i in T}: (sum{(i,j) in E} x[i,j] - sum{(j,i) in E} x[j,i])=-1;
s.t. SinglePath3{i in NoST}: (sum{(i,j) in E} x[i,j] - sum{(j,i) in E} x[j,i])=0;
/*s.t. SinglePath4{(i,j) in E}: x[i,j]+x[j,i]<=1;*/

/* Single path constraints (1).*/

s.t. SlotOfLinkConstraint1{(i,j) in E}: sum{k in MSet} f[i,j,k]=x[i,j]*B;
/* Single link continue constraints of slots (2.1).*/

/*s.t. SlotOfLinkConstraint01{(i,j) in E}: sum{k in AlloSlotsSet} f[i,j,k]=x[i,j]*B;*/




s.t. SlotOfLinkConstraint4{p in MBSet,(i,j) in E}: sum{k in BMatch[p]} f[i,j,k]<=x[i,j]*B;
/* Single link continue constraints of slots (2.4).*/

s.t. SlotOfLinkConstraint5{(i,j) in E,k in MSet}: f[i,j,k]+e[i,j,k]<=1;
/* Single link continue constraints of slots (3).*/

s.t. con{(i,j) in E,k in MBSet}:(f[i,j,k+1]-f[i,j,k]+1)*B>=sum{p in PMSet[k]}f[i,j,p];
s.t. con1{k in MSet,i in NoST}:sum{j in O[i]}f[i,j,k]=sum{j in I[i]}f[j,i,k];

data;

set V:=0 1 2 3 4 5;                     /*The nodes of graphic;*/

set E:=
1 0
1 2
2 0
0 2
2 3
3 2
0 4
4 0
3 4
4 3
3 5
4 5
;
/*
0 1
2 1
5 3
5 4
*/

set S:=1;
set T:=5;
set NoST:=0 2 3 4;
set BSet:=0 1 2;
set MSet:=0 1 2 3 4 5; 
set MBSet:=3; 
set BMatch[0]:=0 1 2;
set BMatch[1]:=1 2 3;
set BMatch[2]:=2 3 4;
set BMatch[3]:=3 4 5;

set I[0]:=1 2 4;
set I[1]:=;
set I[2]:=0 1 3;
set I[3]:=2 4;
set I[4]:=0 3;
set I[5]:=3 4;
set O[0]:=2 4;
set O[1]:=0 2;
set O[2]:=0 3;
set O[3]:=2 4 5;
set O[4]:=0 3 5;
set O[5]:=;

/*set AlloSlotsSet; */
/*set PSet:=0 1 2 3; PSet={0,1,...,M-3}*/
set PMSet[0]:=2 3 4;
set PMSet[1]:=3 4 5;
set PMSet[2]:=4 5;
set PMSet[3]:=5;

/*set PMSet{i in PSet};*/

/*param e{(i,j) in E,k in MSet}, binary;*/
param e:=
1 0 0 1
1 0 1 0
1 0 2 0
1 0 3 1
1 0 4 0
1 0 5 0
1 2 0 0
1 2 1 0
1 2 2 0
1 2 3 1
1 2 4 0
1 2 5 0
2 0 0 0
2 0 1 0
2 0 2 0
2 0 3 0
2 0 4 0
2 0 5 0
0 2 0 0
0 2 1 0
0 2 2 0
0 2 3 0
0 2 4 0
0 2 5 0
3 2 0 0
3 2 1 0
3 2 2 0
3 2 3 0
3 2 4 0
3 2 5 0
2 3 0 0
2 3 1 0
2 3 2 0
2 3 3 0
2 3 4 0
2 3 5 0
0 4 0 0
0 4 1 0
0 4 2 0
0 4 3 0
0 4 4 0
0 4 5 0
4 0 0 0
4 0 1 0
4 0 2 0
4 0 3 0
4 0 4 0
4 0 5 0
3 4 0 0
3 4 1 0
3 4 2 0
3 4 3 0
3 4 4 0
3 4 5 0
4 3 0 0
4 3 1 0
4 3 2 0
4 3 3 0
4 3 4 0
4 3 5 0
4 5 0 0
4 5 1 0
4 5 2 0
4 5 3 0
4 5 4 0
4 5 5 0
3 5 0 0
3 5 1 0
3 5 2 0
3 5 3 0
3 5 4 0
3 5 5 0
;
/*
5 3 0 0
5 3 1 0
5 3 2 0
5 3 3 0
5 3 4 0
5 3 5 0
5 4 0 0
5 4 1 0
5 4 2 0
5 4 3 0
5 4 4 0
5 4 5 0
0 1 0 0
0 1 1 0
0 1 2 0
0 1 3 0
0 1 4 0
0 1 5 0
2 1 0 0
2 1 1 0
2 1 2 0
2 1 3 0
2 1 4 0
2 1 5 0
*/
param B:=3;
param M:=6;


end;