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


end;