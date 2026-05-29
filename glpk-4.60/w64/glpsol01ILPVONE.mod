set V;
set Vv;
set Vf;
set Vnf;

set Ev, within Vv cross Vv;
check{(i,j) in Ev}: i != j;

set E, within V cross V;
check{(i,j) in E}: i != j;


set MSet;
set MBSet;

set I{i in V};
set O{i in V};


param e{(i,j) in E,k in MSet}, binary;
param B{(m,n) in Ev};
param Cv{i in Vv};
param Cs{i in Vf};
param Degree{m in Vv};

set Vvlink{i in Vv};/**与虚拟节点i连接的链路的节点集合***/

set PMSet{(i,j) in E,(m,n) in Ev,k in MSet};
set BMatch{(i,j) in E,(m,n) in Ev,p in MSet};

set VF{i in V};/**物理节点i可以分配的物理节点Vf集合***/
set VV{i in Vv};/**？？？？？？？？？虚拟节点i可以分配的虚拟节点集合吗？感觉不对。***/
set EEx{i in V}, within V cross V;


var x{(i,j) in E,(m,n) in Ev,s in Vf,t in Vf}, binary;
/*Decide if the link (i,j) is selected to the shortest path. */

var f{(i,j) in E,k in MSet}, binary;
/*Decide if the slot k of the link (i,j) is selected to the shortest path. */

var y{(m,n) in Ev,i in Vf,j in Vf}, binary;

/*var zx{(i,j) in E}, integer;*/

/*var c, integer;*/

/*var z{(i,j) in E,(j,p) in E}, binary; */
/*Decide if the links (i,j) and (j,p) are selected to the shortest path. */

/*----------------start Objective Function:min Shortest Path-------------------------------------------------------*/
/*minimize shPath: sum{(i,j) in E} x[i,j];*/
/*minimize shPath: c+sum{(i,j) in E} x[i,j];*/
minimize shPath: sum{(i,j) in E,(m,n) in Ev,s in Vf,t in Vf} x[i,j,m,n,s,t];


/*----------------start Single Path Embedding Constraint-------------------------------------------------------*/
/*s.t. SinglePath1{m in Vv,n in Vvlink[m],s in Vf}: (sum{(s,j) in E,t in Vf} x[s,j,m,n,s,t] - sum{(j,s) in E,t in Vf} x[j,s,m,n,s,t])=y[m,n,s,t];*/

/*s.t. SinglePath11{m in Vv,n in Vvlink[m],s in Vf}: (sum{(s,j) in E,t in Vf} x[s,j,m,n,s,t] - sum{(j,s) in E,t in Vf} x[j,s,m,n,s,t])=y[n,s];*/

/*s.t. SinglePath1{(m,n) in Ev,i in V}: (sum{(i,j) in E,s in VF[i],t in Vf} x[i,j,m,n,s,t] - sum{(j,i) in E,s in VF[i],t in Vf} x[j,i,m,n,s,t])=sum{s in VF[i],p in VV[m]}y[p,s];*/
/*s.t. SinglePath2{(m,n) in Ev,i in V}: (sum{(i,j) in E,s in Vf,t in VF[i]} x[i,j,m,n,t,s] - sum{(j,i) in E,s in Vf,t in VF[i]} x[j,i,m,n,s,t])=-sum{t in VF[i],p in VV[m]}y[p,t];*/
/*s.t. SinglePath3{t in Vf}: (sum{(m,n) in Ev,s in Vf,(i,j) in EEx[t]} x[i,j,m,n,s,t] - sum{(m,n) in Ev,s in Vf,(j,i) in EEx[t]} x[j,i,m,n,s,t])=0;*/
/*s.t. SinglePath301{s in Vf}: (sum{(m,n) in Ev,(i,j) in EEx[s],t in Vf} x[i,j,m,n,s,t] - sum{(m,n) in Ev,(j,i) in EEx[s],t in Vf} x[j,i,m,n,s,t])=0;*/
/*-----------------end------------------------------------------------------*/


/*--------------start Sub-carrier Consecutivess Constraint---------------------------------------------------------*/
/*s.t. SlotOfLinkConstraint1{(i,j) in E,s in Vf,t in Vf}: sum{k in MSet} f[i,j,k]=sum{(m,n) in Ev}x[i,j,m,n,s,t]*B[m,n];*/
/*s.t. SlotOfLinkConstraint101{(m,n) in Ev,k in MBSet,(i,j) in E}: (f[i,j,k+1]-f[i,j,k]+1)*B[m,n]>=sum{p in PMSet[i,j,m,n,k]}f[i,j,p];*/
/*-----------------end------------------------------------------------------*/


/*-------------start Spectrum Continuity Constraint----------------------------------------------------------*/
/*s.t. con1{k in MSet,i in Vnf}:sum{j in O[i]}f[i,j,k]=sum{j in I[i]}f[j,i,k];*/
/*s.t. SlotOfLinkConstraint4{(m,n) in Ev,p in MSet,(i,j) in E,s in Vf,t in Vf}: sum{k in BMatch[i,j,m,n,p]} f[i,j,k]<=x[i,j,m,n,s,t]*B[m,n];*/
/*------------------end-----------------------------------------------------*/

/*-------------start Sub-carrier Capacity Constraint----------------------------------------------------------*/
/*s.t. SlotOfLinkConstraint5{k in MSet,(i,j) in E}: f[i,j,k]+e[i,j,k]<=1;*/
/*------------------end-----------------------------------------------------*/

/*-------------start Variable Constraint----------------------------------------------------------*/
s.t. VariableCon01{(m,n) in Ev,(i,j) in E,s in Vf}: x[i,j,m,n,s,s]=0;
/*------------------end-----------------------------------------------------*/

/*-------------start Node Embedding Constraint----------------------------------------------------------*/
s.t. NodeEmbeddingCon01{(m,n) in Ev,s in Vf,t in Vf}: y[m,n,s,t]*Cv[m]<=Cs[s];
s.t. NodeEmbeddingCon05{(m,n) in Ev,s in Vf,t in Vf}: y[m,n,s,t]*Cv[n]<=Cs[t];

s.t. NodeEmbeddingCon02{s in Vf,t in Vf}: sum{(m,n) in Ev}y[m,n,s,t]<=1;

s.t. NodeEmbeddingCon08{(m,n) in Ev}: sum{s in Vf,t in Vf}(y[m,n,s,t])=1;

s.t. NodeEmbeddingCon07{(m,n) in Ev,s in Vf}: y[m,n,s,s]=0;

/*s.t. NodeEmbeddingCon09{m in Vv,(m,n) in Ev,s in Vf,t in Vf}: sum{(m,nn) in Ev,tt in Vf}y[m,nn,s,tt]=y[m,n,s,t]*Degree[m];*/

/*s.t. NodeEmbeddingCon03: sum{(m,n) in Ev,s in Vf,t in Vf}y[m,n,s,t]*(Cv[m]+Cv[n])=sum{(m,n) in Ev}(Cv[m]+Cv[n]);*/

/*s.t. NodeEmbeddingCon04{(m,n) in Ev,t in Vf}: sum{s in Vf}(y[m,n,s,t])=1;*/
/*s.t. NodeEmbeddingCon06{(m,n) in Ev,s in Vf}: sum{t in Vf}(y[m,n,s,t])=1;*/
/*------------------end-----------------------------------------------------*/

data;

set V:=0 1 2 3 4 5 6 7 8 9;                     /*The nodes of graphic;*/

set VF[0]:=;
set VF[1]:=;
set VF[2]:=;
set VF[3]:=;
set VF[4]:=;
set VF[5]:=;
set VF[6]:=6;
set VF[7]:=7;
set VF[8]:=8;
set VF[9]:=9;

set VV[0]:=0 1 2;
set VV[1]:=0 1 2;
set VV[2]:=0 1 2;

set Vvlink[0]:=1 2;
set Vvlink[1]:=;
set Vvlink[2]:=;

set E:=
1 0
0 1
1 2
2 1
2 0
0 2
2 3
3 2
0 4
4 0
3 4
4 3
3 5
5 3
4 5
5 4
0 6
6 0
2 7
7 2
5 8
8 5
3 9
9 3
;
set Vv:=0 1 2;
set Vf:=6 7 8 9;
set Vnf:=0 1 2 3 4 5;

set Ev:=
0 1
0 2
;



/*minimize shPath: sum{(i,j) in E,(m,n) in Ev,s in Vf,t in Vf} x[i,j,m,n,s,t];*/
/*s.t. SinglePath1{(m,n) in Ev,s in Vf,t in Vf}: (sum{(s,j) in E} x[s,j,m,n,s,t] - sum{(j,s) in E} x[j,s,m,n,s,t])=sum{m in Vv}y[s,m];*/
/*s.t. SinglePath2{(m,n) in Ev,s in Vf,t in Vf}: (sum{(t,j) in E} x[i,j,m,n,s,t] - sum{(j,t) in E} x[j,t,m,n,s,t])=-sum{n in Vv}y[t,n];*/
/*s.t. SinglePath3{(m,n) in Ev,s in Vf,t in Vf}: (sum{(i,j) in E} x[i,j,m,n,s,t] - sum{(j,i) in E} x[j,i,m,n,s,t])=0;*/
/*s.t. SlotOfLinkConstraint1{(i,j) in E,s in Vf,t in Vf}: sum{k in MSet} f[i,j,k]=sum{(m,n) in Ev}x[i,j,m,n,s,t]*B[m,n];*/
/*s.t. SlotOfLinkConstraint4{(m,n) in Ev,p in MSet,(i,j) in E}: sum{k in BMatch[i][j][m][n][p]} f[i,j,k]<=x[i,j]*B[m,n];*/
/*s.t. VariableCon01{(m,n) in Ev,(i,j) in E,s in Vf}: x[i,j,m,n,s,s]=0;*/
/*s.t. SlotOfLinkConstraint5{k in MSet,(i,j) in E}: f[i,j,k]+e[i,j,k]<=1;*/
/*s.t. con1{k in MSet,(i,j) in E}:sum{j in O[i]}f[i,j,k]=sum{j in I[i]}f[j,i,k];*/

param B:=
0 1 2
0 2 3
;
param Degree:=
0 2
1 1
2 1
;

param Cv:=
0 1
1 1
2 1
;

param Cs:=
6 2
7 2
8 2
9 2
;


set MSet:=0 1 2 3 4 5; 
set MBSet:=0 1 2 3 4; 

set EEx[0]:=
1 2
2 1
2 3
3 2
3 4
4 3
3 5
5 3
4 5
5 4
2 7
7 2
5 8
8 5
3 9
9 3
;

set EEx[1]:=
2 0
0 2
2 3
3 2
0 4
4 0
3 4
4 3
3 5
5 3
4 5
5 4
0 6
6 0
2 7
7 2
5 8
8 5
3 9
9 3
;


set EEx[2]:=
1 0
0 1
0 4
4 0
3 4
4 3
3 5
5 3
4 5
5 4
0 6
6 0
5 8
8 5
3 9
9 3
;

set EEx[3]:=
1 2
2 1
4 5
5 4
2 7
7 2
5 8
8 5
;

set EEx[4]:=
1 0
0 1
1 2
2 1
2 0
0 2
2 3
3 2
3 5
5 3
0 6
6 0
2 7
7 2
5 8
8 5
3 9
9 3
;



set EEx[5]:=
1 0
0 1
1 2
2 1
2 0
0 2
2 3
3 2
0 4
4 0
3 4
4 3
0 6
6 0
2 7
7 2
3 9
9 3
;

set EEx[6]:=
1 0
0 1
1 2
2 1
2 0
0 2
2 3
3 2
0 4
4 0
3 4
4 3
3 5
5 3
4 5
5 4
2 7
7 2
5 8
8 5
3 9
9 3
;

set EEx[7]:=
1 0
0 1
1 2
2 1
2 0
0 2
2 3
3 2
0 4
4 0
3 4
4 3
3 5
5 3
4 5
5 4
0 6
6 0
5 8
8 5
3 9
9 3
;

set EEx[8]:=
1 0
0 1
1 2
2 1
2 0
0 2
2 3
3 2
0 4
4 0
3 4
4 3
3 5
5 3
4 5
5 4
0 6
6 0
2 7
7 2
3 9
9 3
;

set EEx[9]:=
1 0
0 1
1 2
2 1
2 0
0 2
2 3
3 2
0 4
4 0
3 4
4 3
3 5
5 3
4 5
5 4
0 6
6 0
2 7
7 2
5 8
8 5
;

/**************set BMatch[1,0,0,1,0]:=0 1;********************************************************/



set BMatch[1,0,0,1,0]:=0 1;
set BMatch[1,0,0,1,1]:=1 2;
set BMatch[1,0,0,1,2]:=2 3;
set BMatch[1,0,0,1,3]:=3 4;
set BMatch[1,0,0,1,4]:=4 5;
set BMatch[1,0,0,1,5]:=;

set BMatch[1,0,0,2,0]:=0 1 2;
set BMatch[1,0,0,2,1]:=1 2 3;
set BMatch[1,0,0,2,2]:=2 3 4;
set BMatch[1,0,0,2,3]:=3 4 5;
set BMatch[1,0,0,2,4]:=;
set BMatch[1,0,0,2,5]:=;

set BMatch[0,1,0,1,0]:=0 1;
set BMatch[0,1,0,1,1]:=1 2;
set BMatch[0,1,0,1,2]:=2 3;
set BMatch[0,1,0,1,3]:=3 4;
set BMatch[0,1,0,1,4]:=4 5;
set BMatch[0,1,0,1,5]:=;

set BMatch[0,1,0,2,0]:=0 1 2;
set BMatch[0,1,0,2,1]:=1 2 3;
set BMatch[0,1,0,2,2]:=2 3 4;
set BMatch[0,1,0,2,3]:=3 4 5;
set BMatch[0,1,0,2,4]:=;
set BMatch[0,1,0,2,5]:=;

set BMatch[1,2,0,1,0]:=0 1;
set BMatch[1,2,0,1,1]:=1 2;
set BMatch[1,2,0,1,2]:=2 3;
set BMatch[1,2,0,1,3]:=3 4;
set BMatch[1,2,0,1,4]:=4 5;
set BMatch[1,2,0,1,5]:=;

set BMatch[1,2,0,2,0]:=0 1 2;
set BMatch[1,2,0,2,1]:=1 2 3;
set BMatch[1,2,0,2,2]:=2 3 4;
set BMatch[1,2,0,2,3]:=3 4 5;
set BMatch[1,2,0,2,4]:=;
set BMatch[1,2,0,2,5]:=;

set BMatch[2,1,0,1,0]:=0 1;
set BMatch[2,1,0,1,1]:=1 2;
set BMatch[2,1,0,1,2]:=2 3;
set BMatch[2,1,0,1,3]:=3 4;
set BMatch[2,1,0,1,4]:=4 5;
set BMatch[2,1,0,1,5]:=;

set BMatch[2,1,0,2,0]:=0 1 2;
set BMatch[2,1,0,2,1]:=1 2 3;
set BMatch[2,1,0,2,2]:=2 3 4;
set BMatch[2,1,0,2,3]:=3 4 5;
set BMatch[2,1,0,2,4]:=;
set BMatch[2,1,0,2,5]:=;

set BMatch[2,0,0,1,0]:=0 1;
set BMatch[2,0,0,1,1]:=1 2;
set BMatch[2,0,0,1,2]:=2 3;
set BMatch[2,0,0,1,3]:=3 4;
set BMatch[2,0,0,1,4]:=4 5;
set BMatch[2,0,0,1,5]:=;

set BMatch[2,0,0,2,0]:=0 1 2;
set BMatch[2,0,0,2,1]:=1 2 3;
set BMatch[2,0,0,2,2]:=2 3 4;
set BMatch[2,0,0,2,3]:=3 4 5;
set BMatch[2,0,0,2,4]:=;
set BMatch[2,0,0,2,5]:=;

set BMatch[0,2,0,1,0]:=0 1;
set BMatch[0,2,0,1,1]:=1 2;
set BMatch[0,2,0,1,2]:=2 3;
set BMatch[0,2,0,1,3]:=3 4;
set BMatch[0,2,0,1,4]:=4 5;
set BMatch[0,2,0,1,5]:=;

set BMatch[0,2,0,2,0]:=0 1 2;
set BMatch[0,2,0,2,1]:=1 2 3;
set BMatch[0,2,0,2,2]:=2 3 4;
set BMatch[0,2,0,2,3]:=3 4 5;
set BMatch[0,2,0,2,4]:=;
set BMatch[0,2,0,2,5]:=;


set BMatch[2,3,0,1,0]:=0 1;
set BMatch[2,3,0,1,1]:=1 2;
set BMatch[2,3,0,1,2]:=2 3;
set BMatch[2,3,0,1,3]:=3 4;
set BMatch[2,3,0,1,4]:=4 5;
set BMatch[2,3,0,1,5]:=;

set BMatch[2,3,0,2,0]:=0 1 2;
set BMatch[2,3,0,2,1]:=1 2 3;
set BMatch[2,3,0,2,2]:=2 3 4;
set BMatch[2,3,0,2,3]:=3 4 5;
set BMatch[2,3,0,2,4]:=;
set BMatch[2,3,0,2,5]:=;

set BMatch[3,2,0,1,0]:=0 1;
set BMatch[3,2,0,1,1]:=1 2;
set BMatch[3,2,0,1,2]:=2 3;
set BMatch[3,2,0,1,3]:=3 4;
set BMatch[3,2,0,1,4]:=4 5;
set BMatch[3,2,0,1,5]:=;

set BMatch[3,2,0,2,0]:=0 1 2;
set BMatch[3,2,0,2,1]:=1 2 3;
set BMatch[3,2,0,2,2]:=2 3 4;
set BMatch[3,2,0,2,3]:=3 4 5;
set BMatch[3,2,0,2,4]:=;
set BMatch[3,2,0,2,5]:=;

set BMatch[4,0,0,1,0]:=0 1;
set BMatch[4,0,0,1,1]:=1 2;
set BMatch[4,0,0,1,2]:=2 3;
set BMatch[4,0,0,1,3]:=3 4;
set BMatch[4,0,0,1,4]:=4 5;
set BMatch[4,0,0,1,5]:=;

set BMatch[4,0,0,2,0]:=0 1 2;
set BMatch[4,0,0,2,1]:=1 2 3;
set BMatch[4,0,0,2,2]:=2 3 4;
set BMatch[4,0,0,2,3]:=3 4 5;
set BMatch[4,0,0,2,4]:=;
set BMatch[4,0,0,2,5]:=;

set BMatch[0,4,0,1,0]:=0 1;
set BMatch[0,4,0,1,1]:=1 2;
set BMatch[0,4,0,1,2]:=2 3;
set BMatch[0,4,0,1,3]:=3 4;
set BMatch[0,4,0,1,4]:=4 5;
set BMatch[0,4,0,1,5]:=;

set BMatch[0,4,0,2,0]:=0 1 2;
set BMatch[0,4,0,2,1]:=1 2 3;
set BMatch[0,4,0,2,2]:=2 3 4;
set BMatch[0,4,0,2,3]:=3 4 5;
set BMatch[0,4,0,2,4]:=;
set BMatch[0,4,0,2,5]:=;


set BMatch[4,3,0,1,0]:=0 1;
set BMatch[4,3,0,1,1]:=1 2;
set BMatch[4,3,0,1,2]:=2 3;
set BMatch[4,3,0,1,3]:=3 4;
set BMatch[4,3,0,1,4]:=4 5;
set BMatch[4,3,0,1,5]:=;

set BMatch[4,3,0,2,0]:=0 1 2;
set BMatch[4,3,0,2,1]:=1 2 3;
set BMatch[4,3,0,2,2]:=2 3 4;
set BMatch[4,3,0,2,3]:=3 4 5;
set BMatch[4,3,0,2,4]:=;
set BMatch[4,3,0,2,5]:=;

set BMatch[3,4,0,1,0]:=0 1;
set BMatch[3,4,0,1,1]:=1 2;
set BMatch[3,4,0,1,2]:=2 3;
set BMatch[3,4,0,1,3]:=3 4;
set BMatch[3,4,0,1,4]:=4 5;
set BMatch[3,4,0,1,5]:=;

set BMatch[3,4,0,2,0]:=0 1 2;
set BMatch[3,4,0,2,1]:=1 2 3;
set BMatch[3,4,0,2,2]:=2 3 4;
set BMatch[3,4,0,2,3]:=3 4 5;
set BMatch[3,4,0,2,4]:=;
set BMatch[3,4,0,2,5]:=;


set BMatch[5,3,0,1,0]:=0 1;
set BMatch[5,3,0,1,1]:=1 2;
set BMatch[5,3,0,1,2]:=2 3;
set BMatch[5,3,0,1,3]:=3 4;
set BMatch[5,3,0,1,4]:=4 5;
set BMatch[5,3,0,1,5]:=;

set BMatch[5,3,0,2,0]:=0 1 2;
set BMatch[5,3,0,2,1]:=1 2 3;
set BMatch[5,3,0,2,2]:=2 3 4;
set BMatch[5,3,0,2,3]:=3 4 5;
set BMatch[5,3,0,2,4]:=;
set BMatch[5,3,0,2,5]:=;

set BMatch[3,5,0,1,0]:=0 1;
set BMatch[3,5,0,1,1]:=1 2;
set BMatch[3,5,0,1,2]:=2 3;
set BMatch[3,5,0,1,3]:=3 4;
set BMatch[3,5,0,1,4]:=4 5;
set BMatch[3,5,0,1,5]:=;

set BMatch[3,5,0,2,0]:=0 1 2;
set BMatch[3,5,0,2,1]:=1 2 3;
set BMatch[3,5,0,2,2]:=2 3 4;
set BMatch[3,5,0,2,3]:=3 4 5;
set BMatch[3,5,0,2,4]:=;
set BMatch[3,5,0,2,5]:=;

set BMatch[5,4,0,1,0]:=0 1;
set BMatch[5,4,0,1,1]:=1 2;
set BMatch[5,4,0,1,2]:=2 3;
set BMatch[5,4,0,1,3]:=3 4;
set BMatch[5,4,0,1,4]:=4 5;
set BMatch[5,4,0,1,5]:=;

set BMatch[5,4,0,2,0]:=0 1 2;
set BMatch[5,4,0,2,1]:=1 2 3;
set BMatch[5,4,0,2,2]:=2 3 4;
set BMatch[5,4,0,2,3]:=3 4 5;
set BMatch[5,4,0,2,4]:=;
set BMatch[5,4,0,2,5]:=;

set BMatch[4,5,0,1,0]:=0 1;
set BMatch[4,5,0,1,1]:=1 2;
set BMatch[4,5,0,1,2]:=2 3;
set BMatch[4,5,0,1,3]:=3 4;
set BMatch[4,5,0,1,4]:=4 5;
set BMatch[4,5,0,1,5]:=;

set BMatch[4,5,0,2,0]:=0 1 2;
set BMatch[4,5,0,2,1]:=1 2 3;
set BMatch[4,5,0,2,2]:=2 3 4;
set BMatch[4,5,0,2,3]:=3 4 5;
set BMatch[4,5,0,2,4]:=;
set BMatch[4,5,0,2,5]:=;


set BMatch[6,0,0,1,0]:=0 1;
set BMatch[6,0,0,1,1]:=1 2;
set BMatch[6,0,0,1,2]:=2 3;
set BMatch[6,0,0,1,3]:=3 4;
set BMatch[6,0,0,1,4]:=4 5;
set BMatch[6,0,0,1,5]:=;

set BMatch[6,0,0,2,0]:=0 1 2;
set BMatch[6,0,0,2,1]:=1 2 3;
set BMatch[6,0,0,2,2]:=2 3 4;
set BMatch[6,0,0,2,3]:=3 4 5;
set BMatch[6,0,0,2,4]:=;
set BMatch[6,0,0,2,5]:=;

set BMatch[0,6,0,1,0]:=0 1;
set BMatch[0,6,0,1,1]:=1 2;
set BMatch[0,6,0,1,2]:=2 3;
set BMatch[0,6,0,1,3]:=3 4;
set BMatch[0,6,0,1,4]:=4 5;
set BMatch[0,6,0,1,5]:=;

set BMatch[0,6,0,2,0]:=0 1 2;
set BMatch[0,6,0,2,1]:=1 2 3;
set BMatch[0,6,0,2,2]:=2 3 4;
set BMatch[0,6,0,2,3]:=3 4 5;
set BMatch[0,6,0,2,4]:=;
set BMatch[0,6,0,2,5]:=;


set BMatch[2,7,0,1,0]:=0 1;
set BMatch[2,7,0,1,1]:=1 2;
set BMatch[2,7,0,1,2]:=2 3;
set BMatch[2,7,0,1,3]:=3 4;
set BMatch[2,7,0,1,4]:=4 5;
set BMatch[2,7,0,1,5]:=;

set BMatch[2,7,0,2,0]:=0 1 2;
set BMatch[2,7,0,2,1]:=1 2 3;
set BMatch[2,7,0,2,2]:=2 3 4;
set BMatch[2,7,0,2,3]:=3 4 5;
set BMatch[2,7,0,2,4]:=;
set BMatch[2,7,0,2,5]:=;

set BMatch[7,2,0,1,0]:=0 1;
set BMatch[7,2,0,1,1]:=1 2;
set BMatch[7,2,0,1,2]:=2 3;
set BMatch[7,2,0,1,3]:=3 4;
set BMatch[7,2,0,1,4]:=4 5;
set BMatch[7,2,0,1,5]:=;

set BMatch[7,2,0,2,0]:=0 1 2;
set BMatch[7,2,0,2,1]:=1 2 3;
set BMatch[7,2,0,2,2]:=2 3 4;
set BMatch[7,2,0,2,3]:=3 4 5;
set BMatch[7,2,0,2,4]:=;
set BMatch[7,2,0,2,5]:=;

set BMatch[5,8,0,1,0]:=0 1;
set BMatch[5,8,0,1,1]:=1 2;
set BMatch[5,8,0,1,2]:=2 3;
set BMatch[5,8,0,1,3]:=3 4;
set BMatch[5,8,0,1,4]:=4 5;
set BMatch[5,8,0,1,5]:=;

set BMatch[5,8,0,2,0]:=0 1 2;
set BMatch[5,8,0,2,1]:=1 2 3;
set BMatch[5,8,0,2,2]:=2 3 4;
set BMatch[5,8,0,2,3]:=3 4 5;
set BMatch[5,8,0,2,4]:=;
set BMatch[5,8,0,2,5]:=;

set BMatch[8,5,0,1,0]:=0 1;
set BMatch[8,5,0,1,1]:=1 2;
set BMatch[8,5,0,1,2]:=2 3;
set BMatch[8,5,0,1,3]:=3 4;
set BMatch[8,5,0,1,4]:=4 5;
set BMatch[8,5,0,1,5]:=;

set BMatch[8,5,0,2,0]:=0 1 2;
set BMatch[8,5,0,2,1]:=1 2 3;
set BMatch[8,5,0,2,2]:=2 3 4;
set BMatch[8,5,0,2,3]:=3 4 5;
set BMatch[8,5,0,2,4]:=;
set BMatch[8,5,0,2,5]:=;

set BMatch[9,3,0,1,0]:=0 1;
set BMatch[9,3,0,1,1]:=1 2;
set BMatch[9,3,0,1,2]:=2 3;
set BMatch[9,3,0,1,3]:=3 4;
set BMatch[9,3,0,1,4]:=4 5;
set BMatch[9,3,0,1,5]:=;

set BMatch[9,3,0,2,0]:=0 1 2;
set BMatch[9,3,0,2,1]:=1 2 3;
set BMatch[9,3,0,2,2]:=2 3 4;
set BMatch[9,3,0,2,3]:=3 4 5;
set BMatch[9,3,0,2,4]:=;
set BMatch[9,3,0,2,5]:=;

set BMatch[3,9,0,1,0]:=0 1;
set BMatch[3,9,0,1,1]:=1 2;
set BMatch[3,9,0,1,2]:=2 3;
set BMatch[3,9,0,1,3]:=3 4;
set BMatch[3,9,0,1,4]:=4 5;
set BMatch[3,9,0,1,5]:=;

set BMatch[3,9,0,2,0]:=0 1 2;
set BMatch[3,9,0,2,1]:=1 2 3;
set BMatch[3,9,0,2,2]:=2 3 4;
set BMatch[3,9,0,2,3]:=3 4 5;
set BMatch[3,9,0,2,4]:=;
set BMatch[3,9,0,2,5]:=;

/**********************************************************************/
set PMSet[1,0,0,1,0]:=2 3;
set PMSet[1,0,0,1,1]:=3 4;
set PMSet[1,0,0,1,2]:=4 5;
set PMSet[1,0,0,1,3]:=5;
set PMSet[1,0,0,1,4]:=;
set PMSet[1,0,0,1,5]:=;

set PMSet[1,0,0,2,0]:=2 3 4;
set PMSet[1,0,0,2,1]:=3 4 5;
set PMSet[1,0,0,2,2]:=3 4;
set PMSet[1,0,0,2,3]:=5;
set PMSet[1,0,0,2,4]:=;
set PMSet[1,0,0,2,5]:=;

set PMSet[0,1,0,1,0]:=2 3;
set PMSet[0,1,0,1,1]:=3 4;
set PMSet[0,1,0,1,2]:=4 5;
set PMSet[0,1,0,1,3]:=5;
set PMSet[0,1,0,1,4]:=;
set PMSet[0,1,0,1,5]:=;

set PMSet[0,1,0,2,0]:=2 3 4;
set PMSet[0,1,0,2,1]:=3 4 5;
set PMSet[0,1,0,2,2]:=4 5;
set PMSet[0,1,0,2,3]:=5;
set PMSet[0,1,0,2,4]:=;
set PMSet[0,1,0,2,5]:=;

set PMSet[1,2,0,1,0]:=2 3;
set PMSet[1,2,0,1,1]:=3 4;
set PMSet[1,2,0,1,2]:=4 5;
set PMSet[1,2,0,1,3]:=5;
set PMSet[1,2,0,1,4]:=;
set PMSet[1,2,0,1,5]:=;

set PMSet[1,2,0,2,0]:=2 3 4;
set PMSet[1,2,0,2,1]:=3 4 5;
set PMSet[1,2,0,2,2]:=4 5;
set PMSet[1,2,0,2,3]:=5;
set PMSet[1,2,0,2,4]:=;
set PMSet[1,2,0,2,5]:=;

set PMSet[2,1,0,1,0]:=2 3;
set PMSet[2,1,0,1,1]:=3 4;
set PMSet[2,1,0,1,2]:=4 5;
set PMSet[2,1,0,1,3]:=5;
set PMSet[2,1,0,1,4]:=;
set PMSet[2,1,0,1,5]:=;

set PMSet[2,1,0,2,0]:=4 5;
set PMSet[2,1,0,2,1]:=5;
set PMSet[2,1,0,2,2]:=;
set PMSet[2,1,0,2,3]:=;
set PMSet[2,1,0,2,4]:=;
set PMSet[2,1,0,2,5]:=;

set PMSet[2,0,0,1,0]:=2 3;
set PMSet[2,0,0,1,1]:=3 4;
set PMSet[2,0,0,1,2]:=4 5;
set PMSet[2,0,0,1,3]:=5;
set PMSet[2,0,0,1,4]:=;
set PMSet[2,0,0,1,5]:=;

set PMSet[2,0,0,2,0]:=2 3 4;
set PMSet[2,0,0,2,1]:=3 4 5;
set PMSet[2,0,0,2,2]:=4 5;
set PMSet[2,0,0,2,3]:=5;
set PMSet[2,0,0,2,4]:=;
set PMSet[2,0,0,2,5]:=;

set PMSet[0,2,0,1,0]:=2 3;
set PMSet[0,2,0,1,1]:=3 4;
set PMSet[0,2,0,1,2]:=4 5;
set PMSet[0,2,0,1,3]:=5;
set PMSet[0,2,0,1,4]:=;
set PMSet[0,2,0,1,5]:=;

set PMSet[0,2,0,2,0]:=2 3 4;
set PMSet[0,2,0,2,1]:=3 4 5;
set PMSet[0,2,0,2,2]:=4 5;
set PMSet[0,2,0,2,3]:=5;
set PMSet[0,2,0,2,4]:=;
set PMSet[0,2,0,2,5]:=;


set PMSet[2,3,0,1,0]:=2 3;
set PMSet[2,3,0,1,1]:=3 4;
set PMSet[2,3,0,1,2]:=4 5;
set PMSet[2,3,0,1,3]:=5;
set PMSet[2,3,0,1,4]:=;
set PMSet[2,3,0,1,5]:=;

set PMSet[2,3,0,2,0]:=2 3 4;
set PMSet[2,3,0,2,1]:=3 4 5;
set PMSet[2,3,0,2,2]:=4 5;
set PMSet[2,3,0,2,3]:=5;
set PMSet[2,3,0,2,4]:=;
set PMSet[2,3,0,2,5]:=;

set PMSet[3,2,0,1,0]:=2 3;
set PMSet[3,2,0,1,1]:=3 4;
set PMSet[3,2,0,1,2]:=4 5;
set PMSet[3,2,0,1,3]:=5;
set PMSet[3,2,0,1,4]:=;
set PMSet[3,2,0,1,5]:=;

set PMSet[3,2,0,2,0]:=2 3 4;
set PMSet[3,2,0,2,1]:=3 4 5;
set PMSet[3,2,0,2,2]:=4 5;
set PMSet[3,2,0,2,3]:=5;
set PMSet[3,2,0,2,4]:=;
set PMSet[3,2,0,2,5]:=;

set PMSet[4,0,0,1,0]:=2 3;
set PMSet[4,0,0,1,1]:=3 4;
set PMSet[4,0,0,1,2]:=4 5;
set PMSet[4,0,0,1,3]:=5;
set PMSet[4,0,0,1,4]:=;
set PMSet[4,0,0,1,5]:=;

set PMSet[4,0,0,2,0]:=2 3 4;
set PMSet[4,0,0,2,1]:=3 4 5;
set PMSet[4,0,0,2,2]:=4 5;
set PMSet[4,0,0,2,3]:=5;
set PMSet[4,0,0,2,4]:=;
set PMSet[4,0,0,2,5]:=;

set PMSet[0,4,0,1,0]:=2 3;
set PMSet[0,4,0,1,1]:=3 4;
set PMSet[0,4,0,1,2]:=4 5;
set PMSet[0,4,0,1,3]:=5;
set PMSet[0,4,0,1,4]:=;
set PMSet[0,4,0,1,5]:=;

set PMSet[0,4,0,2,0]:=2 3 4;
set PMSet[0,4,0,2,1]:=3 4 5;
set PMSet[0,4,0,2,2]:=4 5;
set PMSet[0,4,0,2,3]:=5;
set PMSet[0,4,0,2,4]:=;
set PMSet[0,4,0,2,5]:=;


set PMSet[4,3,0,1,0]:=2 3;
set PMSet[4,3,0,1,1]:=3 4;
set PMSet[4,3,0,1,2]:=4 5;
set PMSet[4,3,0,1,3]:=5;
set PMSet[4,3,0,1,4]:=;
set PMSet[4,3,0,1,5]:=;

set PMSet[4,3,0,2,0]:=2 3 4;
set PMSet[4,3,0,2,1]:=3 4 5;
set PMSet[4,3,0,2,2]:=4 5;
set PMSet[4,3,0,2,3]:=5;
set PMSet[4,3,0,2,4]:=;
set PMSet[4,3,0,2,5]:=;

set PMSet[3,4,0,1,0]:=2 3;
set PMSet[3,4,0,1,1]:=3 4;
set PMSet[3,4,0,1,2]:=4 5;
set PMSet[3,4,0,1,3]:=5;
set PMSet[3,4,0,1,4]:=;
set PMSet[3,4,0,1,5]:=;

set PMSet[3,4,0,2,0]:=2 3 4;
set PMSet[3,4,0,2,1]:=3 4 5;
set PMSet[3,4,0,2,2]:=4 5;
set PMSet[3,4,0,2,3]:=5;
set PMSet[3,4,0,2,4]:=;
set PMSet[3,4,0,2,5]:=;


set PMSet[5,3,0,1,0]:=2 3;
set PMSet[5,3,0,1,1]:=3 4;
set PMSet[5,3,0,1,2]:=4 5;
set PMSet[5,3,0,1,3]:=5;
set PMSet[5,3,0,1,4]:=;
set PMSet[5,3,0,1,5]:=;

set PMSet[5,3,0,2,0]:=2 3 4;
set PMSet[5,3,0,2,1]:=3 4 5;
set PMSet[5,3,0,2,2]:=4 5;
set PMSet[5,3,0,2,3]:=5;
set PMSet[5,3,0,2,4]:=;
set PMSet[5,3,0,2,5]:=;

set PMSet[3,5,0,1,0]:=2 3;
set PMSet[3,5,0,1,1]:=3 4;
set PMSet[3,5,0,1,2]:=4 5;
set PMSet[3,5,0,1,3]:=5;
set PMSet[3,5,0,1,4]:=;
set PMSet[3,5,0,1,5]:=;

set PMSet[3,5,0,2,0]:=2 3 4;
set PMSet[3,5,0,2,1]:=3 4 5;
set PMSet[3,5,0,2,2]:=4 5;
set PMSet[3,5,0,2,3]:=5;
set PMSet[3,5,0,2,4]:=;
set PMSet[3,5,0,2,5]:=;

set PMSet[5,4,0,1,0]:=2 3;
set PMSet[5,4,0,1,1]:=3 4;
set PMSet[5,4,0,1,2]:=4 5;
set PMSet[5,4,0,1,3]:=5;
set PMSet[5,4,0,1,4]:=;
set PMSet[5,4,0,1,5]:=;

set PMSet[5,4,0,2,0]:=2 3 4;
set PMSet[5,4,0,2,1]:=3 4 5;
set PMSet[5,4,0,2,2]:=4 5;
set PMSet[5,4,0,2,3]:=5;
set PMSet[5,4,0,2,4]:=;
set PMSet[5,4,0,2,5]:=;

set PMSet[4,5,0,1,0]:=2 3;
set PMSet[4,5,0,1,1]:=3 4;
set PMSet[4,5,0,1,2]:=4 5;
set PMSet[4,5,0,1,3]:=5;
set PMSet[4,5,0,1,4]:=;
set PMSet[4,5,0,1,5]:=;

set PMSet[4,5,0,2,0]:=2 3 4;
set PMSet[4,5,0,2,1]:=3 4 5;
set PMSet[4,5,0,2,2]:=4 5;
set PMSet[4,5,0,2,3]:=5;
set PMSet[4,5,0,2,4]:=;
set PMSet[4,5,0,2,5]:=;


set PMSet[6,0,0,1,0]:=2 3;
set PMSet[6,0,0,1,1]:=3 4;
set PMSet[6,0,0,1,2]:=4 5;
set PMSet[6,0,0,1,3]:=5;
set PMSet[6,0,0,1,4]:=;
set PMSet[6,0,0,1,5]:=;

set PMSet[6,0,0,2,0]:=2 3 4;
set PMSet[6,0,0,2,1]:=3 4 5;
set PMSet[6,0,0,2,2]:=4 5;
set PMSet[6,0,0,2,3]:=5;
set PMSet[6,0,0,2,4]:=;
set PMSet[6,0,0,2,5]:=;

set PMSet[0,6,0,1,0]:=2 3;
set PMSet[0,6,0,1,1]:=3 4;
set PMSet[0,6,0,1,2]:=4 5;
set PMSet[0,6,0,1,3]:=5;
set PMSet[0,6,0,1,4]:=;
set PMSet[0,6,0,1,5]:=;

set PMSet[0,6,0,2,0]:=2 3 4;
set PMSet[0,6,0,2,1]:=3 4 5;
set PMSet[0,6,0,2,2]:=4 5;
set PMSet[0,6,0,2,3]:=5;
set PMSet[0,6,0,2,4]:=;
set PMSet[0,6,0,2,5]:=;


set PMSet[2,7,0,1,0]:=2 3;
set PMSet[2,7,0,1,1]:=3 4;
set PMSet[2,7,0,1,2]:=4 5;
set PMSet[2,7,0,1,3]:=5;
set PMSet[2,7,0,1,4]:=;
set PMSet[2,7,0,1,5]:=;

set PMSet[2,7,0,2,0]:=2 3 4;
set PMSet[2,7,0,2,1]:=3 4 5;
set PMSet[2,7,0,2,2]:=4 5;
set PMSet[2,7,0,2,3]:=5;
set PMSet[2,7,0,2,4]:=;
set PMSet[2,7,0,2,5]:=;

set PMSet[7,2,0,1,0]:=2 3;
set PMSet[7,2,0,1,1]:=3 4;
set PMSet[7,2,0,1,2]:=4 5;
set PMSet[7,2,0,1,3]:=5;
set PMSet[7,2,0,1,4]:=;
set PMSet[7,2,0,1,5]:=;

set PMSet[7,2,0,2,0]:=2 3 4;
set PMSet[7,2,0,2,1]:=3 4 5;
set PMSet[7,2,0,2,2]:=4 5;
set PMSet[7,2,0,2,3]:=5;
set PMSet[7,2,0,2,4]:=;
set PMSet[7,2,0,2,5]:=;

set PMSet[5,8,0,1,0]:=2 3;
set PMSet[5,8,0,1,1]:=3 4;
set PMSet[5,8,0,1,2]:=4 5;
set PMSet[5,8,0,1,3]:=5;
set PMSet[5,8,0,1,4]:=;
set PMSet[5,8,0,1,5]:=;

set PMSet[5,8,0,2,0]:=2 3 4;
set PMSet[5,8,0,2,1]:=3 4 5;
set PMSet[5,8,0,2,2]:=4 5;
set PMSet[5,8,0,2,3]:=5;
set PMSet[5,8,0,2,4]:=;
set PMSet[5,8,0,2,5]:=;

set PMSet[8,5,0,1,0]:=2 3;
set PMSet[8,5,0,1,1]:=3 4;
set PMSet[8,5,0,1,2]:=4 5;
set PMSet[8,5,0,1,3]:=5;
set PMSet[8,5,0,1,4]:=;
set PMSet[8,5,0,1,5]:=;

set PMSet[8,5,0,2,0]:=2 3 4;
set PMSet[8,5,0,2,1]:=3 4 5;
set PMSet[8,5,0,2,2]:=4 5;
set PMSet[8,5,0,2,3]:=5;
set PMSet[8,5,0,2,4]:=;
set PMSet[8,5,0,2,5]:=;

set PMSet[9,3,0,1,0]:=2 3;
set PMSet[9,3,0,1,1]:=3 4;
set PMSet[9,3,0,1,2]:=4 5;
set PMSet[9,3,0,1,3]:=5;
set PMSet[9,3,0,1,4]:=;
set PMSet[9,3,0,1,5]:=;

set PMSet[9,3,0,2,0]:=2 3 4;
set PMSet[9,3,0,2,1]:=3 4 5;
set PMSet[9,3,0,2,2]:=4 5;
set PMSet[9,3,0,2,3]:=5;
set PMSet[9,3,0,2,4]:=;
set PMSet[9,3,0,2,5]:=;

set PMSet[3,9,0,1,0]:=2 3;
set PMSet[3,9,0,1,1]:=3 4;
set PMSet[3,9,0,1,2]:=4 5;
set PMSet[3,9,0,1,3]:=5;
set PMSet[3,9,0,1,4]:=;
set PMSet[3,9,0,1,5]:=;

set PMSet[3,9,0,2,0]:=2 3 4;
set PMSet[3,9,0,2,1]:=3 4 5;
set PMSet[3,9,0,2,2]:=4 5;
set PMSet[3,9,0,2,3]:=5;
set PMSet[3,9,0,2,4]:=;
set PMSet[3,9,0,2,5]:=;
/*******************************************************************/

set I[0]:=1 2 4 6;
set I[1]:=0 2;
set I[2]:=0 1 3 7;
set I[3]:=2 4 5 9;
set I[4]:=0 3 5;
set I[5]:=3 4 8;
set I[6]:=0;
set I[7]:=2;
set I[8]:=5;
set I[9]:=3;
set O[0]:=1 2 4 6;
set O[1]:=0 2;
set O[2]:=0 1 3 7;
set O[3]:=2 4 5 9;
set O[4]:=0 3 5;
set O[5]:=3 4 8;
set O[6]:=0;
set O[7]:=2;
set O[8]:=5;
set O[9]:=3;



/*s.t. SlotOfLinkConstraint5{k in MSet,(i,j) in E}: f[i,j,k]+e[i,j,k]<=1;*/
/*param e{(i,j) in E,k in MSet}, binary;*/
param e:=
1 0 0 0
1 0 1 0
1 0 2 0
1 0 3 0
1 0 4 0
1 0 5 0
0 1 0 0
0 1 1 0
0 1 2 0
0 1 3 0
0 1 4 0
0 1 5 0
1 2 0 0
1 2 1 0
1 2 2 0
1 2 3 0
1 2 4 0
1 2 5 0
2 1 0 0
2 1 1 0
2 1 2 0
2 1 3 0
2 1 4 0
2 1 5 0
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
5 4 0 0
5 4 1 0
5 4 2 0
5 4 3 0
5 4 4 0
5 4 5 0
3 5 0 0
3 5 1 0
3 5 2 0
3 5 3 0
3 5 4 0
3 5 5 0
5 3 0 0
5 3 1 0
5 3 2 0
5 3 3 0
5 3 4 0
5 3 5 0
0 6 0 0
0 6 1 0
0 6 2 0
0 6 3 0
0 6 4 0
0 6 5 0
6 0 0 0
6 0 1 0
6 0 2 0
6 0 3 0
6 0 4 0
6 0 5 0
2 7 0 0
2 7 1 0
2 7 2 0
2 7 3 0
2 7 4 0
2 7 5 0
7 2 0 0
7 2 1 0
7 2 2 0
7 2 3 0
7 2 4 0
7 2 5 0
5 8 0 0
5 8 1 0
5 8 2 0
5 8 3 0
5 8 4 0
5 8 5 0
8 5 0 0
8 5 1 0
8 5 2 0
8 5 3 0
8 5 4 0
8 5 5 0
3 9 0 0
3 9 1 0
3 9 2 0
3 9 3 0
3 9 4 0
3 9 5 0
9 3 0 0
9 3 1 0
9 3 2 0
9 3 3 0
9 3 4 0
9 3 5 0
;




end;