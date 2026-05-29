set V;
set E, within V cross V;
check{(i,j) in E}: i != j;
set S;
set T;
set NoST;
set SSet;/*集合数量*/
set VSet{i in SSet}, within V cross V;/*SSSet[2]=*/
/*set VSet{k in SSet,k1 in SSSet[k]};VSet[2]={E[i,j]},VSet[3]={}*/

param g{(i,j) in E};

param ValueSet{k in SSet};

/*Decide if the link (i,j) is selected to the shortest path. */
var x{(i,j) in E}, binary;

/* The shortest path, that is the single path from s to t (7).*/
minimize shPath: sum{(i,j) in E} x[i,j]*g[i,j];


/* Single path constraints (1).*/
s.t. SinglePath1{i in S}: (sum{(i,j) in E} x[i,j] - sum{(j,i) in E} x[j,i])=1;
s.t. SinglePath2{i in T}: (sum{(i,j) in E} x[i,j] - sum{(j,i) in E} x[j,i])=-1;
s.t. SinglePath3{i in NoST}: (sum{(i,j) in E} x[i,j] - sum{(j,i) in E} x[j,i])=0;
/*No circle path*/
s.t. NoCirclePath{k in SSet}:sum{(i,j) in VSet[k]}x[i,j]<=ValueSet[k]-1;

s.t. SinglePath4: sum{(i,j) in E} x[i,j]>=3;
s.t. SinglePath5{(i,j) in E}: x[i,j]+x[j,i]<=1;



end;