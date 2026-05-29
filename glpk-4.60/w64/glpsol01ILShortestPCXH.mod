set V;
set E, within V cross V;
check{(i,j) in E}: i != j;
set S;
set T;
set NoST;

param g{(i,j) in E};

/*Decide if the link (i,j) is selected to the shortest path. */
var x{(i,j) in E}, binary;

/* The shortest path, that is the single path from s to t (7).*/
minimize shPath: sum{(i,j) in E} x[i,j]*g[i,j];

/* Single path constraints (1).*/
s.t. SinglePath1{i in S}: (sum{(i,j) in E} x[i,j] - sum{(j,i) in E} x[j,i])=1;
s.t. SinglePath2{i in T}: (sum{(i,j) in E} x[i,j] - sum{(j,i) in E} x[j,i])=-1;
s.t. SinglePath3{i in NoST}: (sum{(i,j) in E} x[i,j] - sum{(j,i) in E} x[j,i])=0;

s.t. SinglePath4: sum{(i,j) in E} x[i,j]>=3;
s.t. SinglePath5{(i,j) in E}: x[i,j]+x[j,i]<=1;

data;

set V:=0 1 2 3 4 5 6;                     /*The nodes of graphic;*/

set E:=
0 4
4 0
1 4
4 1
2 4
4 2
3 4
4 3
5 0
0 5
5 1
1 5
5 2
2 5
5 3
3 5
6 0
0 6
6 1
1 6
6 2
2 6
6 3
3 6
;


set S:=5;
set T:=6;
set NoST:=0 1 2 3 4;


/*param g{(i,j) in E};*/
param g:=
0 4 74
4 0 74
1 4 60
4 1 60 
2 4 60
4 2 60
3 4 97
4 3 97
5 0 100
0 5 100
5 1 100
1 5 100
5 2 100
2 5 100
5 3 100
3 5 100
6 0 100
0 6 100
6 1 100
1 6 100
6 2 100
2 6 100
6 3 100
3 6 100
;

end;