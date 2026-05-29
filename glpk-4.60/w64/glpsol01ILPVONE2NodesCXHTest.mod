set V;
set Vv;
set Vf;
set Vnf;/*网络中除去facility节点的节点*/

set Ev, within Vv cross Vv;
check{(i,j) in Ev}: i != j;

set E, within V cross V;
check{(i,j) in E}: i != j;


set MSet;
set MBSet;

set I{i in V};
set O{i in V};


param e{(i,j) in E,k in MSet}, binary;
param B{(m,n) in Ev};/*虚拟链路(m,n)的带宽请求*/
param Cv{i in Vv};
param Cs{i in Vf};
/*param Degree{m in Vv};*/

/*set Vvlink{i in Vv};*/
/**与虚拟节点i连接的链路的节点集合***/

set PMSet{(i,j) in E,(m,n) in Ev,k in MSet};
set PKSet{k in MSet};
set BMatch{(i,j) in E,(m,n) in Ev,p in MSet};

/*set VF{i in V};*/
/**物理节点i可以分配的物理节点Vf集合***/
/*set VV{i in Vv};*/
/**？？？？？？？？？虚拟节点i可以分配的虚拟节点集合吗？感觉不对。***/
/*set EEx{i in V}, within V cross V;*/

var x{(i,j) in E,(m,n) in Ev,s in Vf,t in Vf}, binary;
/*Decide if the link (i,j) is selected to the shortest path. */

var f{(i,j) in E,(m,n) in Ev,s in Vf,t in Vf,k in MSet}, binary;
/*Decide if the slot k of the link (i,j) is selected to the shortest path. */

var y{m in Vv,i in Vf}, binary;

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

s.t. SinglePath1{(m,n) in Ev,s in Vf}: (sum{(s,j) in E,t in Vf} x[s,j,m,n,s,t] - sum{(j,s) in E,t in Vf} x[j,s,m,n,s,t])=y[m,s];
s.t. SinglePath2{(m,n) in Ev,t in Vf}: (sum{(t,j) in E,s in Vf} x[t,j,m,n,s,t] - sum{(j,t) in E,s in Vf} x[j,t,m,n,s,t])=-y[n,t];
s.t. SinglePath3{(m,n) in Ev,i in Vnf,s in Vf,t in Vf}: (sum{(i,j) in E} x[i,j,m,n,s,t] - sum{(j,i) in E} x[j,i,m,n,s,t])=0;

/*s.t. SinglePath1{(m,n) in Ev,i in V}: (sum{(i,j) in E,s in VF[i],t in Vf} x[i,j,m,n,s,t] - sum{(j,i) in E,s in VF[i],t in Vf} x[j,i,m,n,s,t])=sum{s in VF[i],p in VV[m]}y[p,s];*/
/*s.t. SinglePath2{(m,n) in Ev,i in V}: (sum{(i,j) in E,s in Vf,t in VF[i]} x[i,j,m,n,t,s] - sum{(j,i) in E,s in Vf,t in VF[i]} x[j,i,m,n,s,t])=-sum{t in VF[i],p in VV[m]}y[p,t];*/
/*s.t. SinglePath3{t in Vf}: (sum{(m,n) in Ev,s in Vf,(i,j) in EEx[t]} x[i,j,m,n,s,t] - sum{(m,n) in Ev,s in Vf,(j,i) in EEx[t]} x[j,i,m,n,s,t])=0;*/
/*s.t. SinglePath301{s in Vf}: (sum{(m,n) in Ev,(i,j) in EEx[s],t in Vf} x[i,j,m,n,s,t] - sum{(m,n) in Ev,(j,i) in EEx[s],t in Vf} x[j,i,m,n,s,t])=0;*/
/*-----------------end------------------------------------------------------*/


/*--------------start Sub-carrier Consecutivess Constraint---------------------------------------------------------*/
s.t. SlotOfLinkConstraint1{(i,j) in E,(m,n) in Ev,s in Vf,t in Vf}: sum{k in MSet} (f[i,j,m,n,s,t,k])=x[i,j,m,n,s,t]*B[m,n];
/*s.t. SlotOfLinkConstraint101{(i,j) in E,(m,n) in Ev,s in Vf,t in Vf,k in MBSet}: (f[i,j,m,n,s,t,k+1]-f[i,j,m,n,s,t,k]+1)*B[m,n]>=sum{p in PMSet[i,j,m,n,k]}f[i,j,m,n,s,t,p];*/
s.t. SlotOfLinkConstraint101{(i,j) in E,(m,n) in Ev,s in Vf,t in Vf,k in MBSet}: (f[i,j,m,n,s,t,k+1]-f[i,j,m,n,s,t,k]+1)*B[m,n]>=sum{p in PKSet[k+1]}f[i,j,m,n,s,t,p];
s.t. SlotOfLinkConstraint102{(i,j) in E,(m,n) in Ev,s in Vf,k in MSet}: f[i,j,m,n,s,s,k]=0;
/*-----------------end------------------------------------------------------*/


/*-------------start Spectrum Continuity Constraint----------------------------------------------------------*/
s.t. con1{(m,n) in Ev,s in Vf,t in Vf,k in MSet,i in Vnf}:sum{j in O[i]}f[i,j,m,n,s,t,k]=sum{j in I[i]}f[j,i,m,n,s,t,k];
s.t. SlotOfLinkConstraint4{(m,n) in Ev,p in MSet,(i,j) in E,s in Vf,t in Vf}: sum{k in BMatch[i,j,m,n,p]} f[i,j,m,n,s,t,k]<=x[i,j,m,n,s,t]*B[m,n];
/*------------------end-----------------------------------------------------*/

/*-------------start Sub-carrier Capacity Constraint----------------------------------------------------------*/
s.t. SlotOfLinkConstraint5{(m,n) in Ev,k in MSet,(i,j) in E,s in Vf,t in Vf}: f[i,j,m,n,s,t,k]+e[i,j,k]<=1;
/*------------------end-----------------------------------------------------*/

/*-------------start Variable Constraint----------------------------------------------------------*/
s.t. VariableCon01{(m,n) in Ev,(i,j) in E,s in Vf}: x[i,j,m,n,s,s]=0;
/*------------------end-----------------------------------------------------*/

/*-------------start Node Embedding Constraint----------------------------------------------------------*/
s.t. NodeEmbeddingCon01{m in Vv,s in Vf}: y[m,s]*Cv[m]<=Cs[s];

s.t. NodeEmbeddingCon02{s in Vf}: sum{m in Vv}y[m,s]<=1;

s.t. NodeEmbeddingCon08{m in Vv}: sum{s in Vf}(y[m,s])=1;


/*s.t. NodeEmbeddingCon03: sum{(m,n) in Ev,s in Vf,t in Vf}y[m,n,s,t]*(Cv[m]+Cv[n])=sum{(m,n) in Ev}(Cv[m]+Cv[n]);*/

/*s.t. NodeEmbeddingCon04{(m,n) in Ev,t in Vf}: sum{s in Vf}(y[m,n,s,t])=1;*/
/*s.t. NodeEmbeddingCon06{(m,n) in Ev,s in Vf}: sum{t in Vf}(y[m,n,s,t])=1;*/
/*------------------end-----------------------------------------------------*/

end;