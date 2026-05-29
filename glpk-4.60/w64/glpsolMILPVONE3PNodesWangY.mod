set MSet;/*索引集合，{0,1,...,M-1}*/
set D;/*虚拟链路集合*/
set Path;/*路径p的集合*/
set P{d in D};/*虚拟链路d的路径p集合*/
set Nv;/*虚拟节点的集合*/
set F;/*服务器节点（facility nodes）的集合*/
set Na;/*辅助图的节点集合，Na=F并Nv并网络节点*/
set A{u in Nv}, within Na cross Na;/*与虚拟节点u的辅助边的集合*/
set Afa{u in F}, within Na cross Na;/*与物理节点（facility nodes）u的辅助边的集合*/
/*set ASet{u in F};与节点（facility nodes）u的辅助边的集合*/
set FS{d in D,p in Path};/*路径p上可能的起始频谱槽索引集合*/
set Ef, within Na cross Na;/*与服务器节点的辅助边的集合*/
set Af{u in Nv};/*每个虚拟节点可映射的节点集合*/

set Du{u in Nv};/*具有虚拟节点u的虚拟链路集合*/
/*set PathNf{p in Path};路径上的f节点集合*/
/*set PathNv{p in Path};路径上的虚拟节点集合*/
set PathLinks{p in Path}, within Na cross Na;/*路径上的虚拟节点->f节点的链路集合*/
set DNo{d in D};/*除去d的*/

param NSd{d in D,p in Path};/*虚拟网络在路径p上请求的频谱槽数量*/
param H{p in Path};/*路径p的跳数*/
param PNum{(u,v) in Ef};/*经过链路(u,v)的路径数量*/
param fs{d in D,p in Path,i in MSet};/*路径p上的第i个起始频谱槽索引*/
param MSlots;/*最大的频谱槽索引*/
param Sita{p in Path,(u,v) in Ef}, binary;/*二进制变量，*/
param Degree{u in Nv};/*虚拟节点的度*/

var y{d in D,p in Path}, binary;/*二进制变量，如果虚拟网络链路d映射在路径p上，则为1，否则为0*/
var M{u in Nv,v in Af[u]}, binary;/*二进制变量，*/
var E{d in D},integer;/*分配给虚拟网洛的频谱槽终点索引*/
var S{d in D},integer;/*分配给虚拟网洛的频谱槽起始节点索引*/
var B{d in D},integer;/*为虚拟网络分配的频谱槽总的数量*/
var x{d in D,p in Path,i in FS[d,p]}, binary;/*set FS{d in D,p in Path};路径p上可能的起始频谱槽索引集合*/
var Sigma{di in D,dj in D}, binary;
var z{di in D,dj in DNo[di],p in Path}, integer;/*当di分配的slot小于dj分配的slot，E[di]=z[di,dj,p]*/
/*
var yy{d in D,p in Path,(u,v) in Ef}, binary;
var z{d in D,p in Path}, binary;
*/

minimize slotsMin: sum{d in D}sum{p in Path} NSd[d,p]*y[d,p]*(H[p]-2);

/**********start(节点和链路映射)*******************************************************************************/
s.t. NodeAssignmentCon1{u in Nv}: sum{(u,v) in A[u]} M[u,v]=1;
s.t. NodeAssignmentCon2{v in F}: sum{(u,v) in Afa[v]} M[u,v]<=1;
s.t. NodeAssignmentCon3{(u,v) in Ef}: sum{d in D,p in Path} Sita[p,u,v]*y[d,p] >= Degree[u]*M[u,v];/*每条链路的映射的总和大于2倍的节点映射M[u,v]*/

s.t. LinkMappingCon1{d in D}: sum{p in Path} y[d,p]=1;
s.t. LinkMappingCon2{p in Path}: sum{d in D} y[d,p]<=1;/*一条物理路径只能被映射盗一条虚拟链路*/
s.t. LinkMappingCon3{d in D}: sum{p in P[d]} y[d,p]=1;/*一条虚拟链路只能映射一条物理路径*/
s.t. LinkMappingCon4{u in Nv}: sum{d in D,p in Path,(u,v) in Ef} Sita[p,u,v]*y[d,p]=Degree[u];/*一条虚拟链路映射的一条物理路径的链路的总和为2条*/
/**********end***************************************************************************************************/

/**********start(频谱槽分配)*******************************************************************************/
s.t. OpticalRelatedCon10{di in D,dj in DNo[di],p in Path}: z[di,dj,p]>=-1;
s.t. OpticalRelatedCon11{di in D,dj in DNo[di],p in Path}: z[di,dj,p]>=Sigma[di,dj]*(NSd[di,p])-1;/*当sigma==1时，NSd可能=1，则z将可以选择0*/
s.t. OpticalRelatedCon12{di in D,dj in DNo[di],p in Path}: z[di,dj,p]<=(S[dj]-1);
s.t. OpticalRelatedCon121{di in D,dj in DNo[di],p in Path}: z[di,dj,p]<=(E[dj]-1);
s.t. OpticalRelatedCon14{di in D,dj in DNo[di],p in Path}: z[di,dj,p]<=E[di];

/*s.t. OpticalRelatedCon13{di in D,dj in DNo[di],p in Path}: z[di,dj,p]<=(Sigma[di,dj])*(NSd[di,p]+NSd[dj,p]);*/
/*s.t. OpticalRelatedCon131{di in D,dj in DNo[di],p in Path}: z[dj,di,p]<=(Sigma[di,dj])*(NSd[di,p]+NSd[dj,p]);*/
/*s.t. OpticalRelatedCon15{di in D,dj in DNo[di],p in Path}: z[di,dj,p]<=E[dj]-2;*/
/*s.t. OpticalRelatedCon16{p in Path}: sum{di in D,dj in DNo[di]}(z[di,dj,p]-S[di]+1)<=(MSlots);*/

s.t. OpticalRelatedCon1{d in D}: B[d]=sum{p in Path} y[d,p]*NSd[d,p];
s.t. OpticalRelatedCon2{d in D}: E[d]=S[d]+B[d]-1;
s.t. OpticalRelatedCon5{d in D}: E[d]<=MSlots;
s.t. OpticalRelatedCon3{d in D}: S[d]=sum{p in P[d]}sum{i in FS[d,p]}fs[d,p,i]*x[d,p,i];
s.t. OpticalRelatedCon4{d in D,p in P[d]}:sum{i in FS[d,p]}x[d,p,i]=y[d,p];
s.t. OpticalRelatedCon6{di in D,dj in DNo[di]}: Sigma[di,dj] + Sigma[dj,di] = 1;
s.t. OpticalRelatedCon7{di in D,dj in DNo[di],pa in P[di],pb in P[dj]}:E[dj]-S[di]<=(MSlots)*(Sigma[di,dj]+2-y[di,pa]-y[dj,pb]);


/*s.t. OpticalRelatedCon8{di in D,dj in DNo[di],pa in P[di],pb in P[dj]}:E[dj]-E[di]<=NSd[dj,pb]*(Sigma[di,dj]+2-y[di,pa]-y[dj,pb]);*/
/*s.t. OpticalRelatedCon9{di in D,dj in DNo[di],pa in P[di],pb in P[dj]}:E[dj]-E[di]<=NSd[dj,pb]*(Sigma[di,dj]+2-y[di,pa]-y[dj,pb]);*/
/*s.t. OpticalRelatedCon8{di in D,dj in DNo[di],p in Path}:E[di]-E[dj]>=(-1)*NSd[dj,p]*Sigma[dj,di];*/
/**********end***************************************************************************************************/




/*s.t. LinkMappingCon2{u in Nv,(u,v) in A[u]}: sum{d in Du[u],p in Path} Sita[p,u,v]*y[d,p]<=M[u,v]*PNum[u,v];*/
/*s.t. LinkMappingCon4{d in D}: sum{p in Path,(u,v) in Ef} Sita[p,u,v]*y[d,p]=2;一条虚拟链路只能映射一条物理路径的端链路为2条*/

/*s.t. LinkMappingCon5{d in D}: sum{p in Path,(u,v) in Ef} Sita[p,u,v]*yy[d,p,u,v]>=1;*/

/*s.t. LinkMappingCon10{d in D}: sum{p in P[d],(u,v) in Ef} yy[d,p,u,v]=1;*/

/*s.t. LinkMappingCon7{p in Path}: sum{d in D,(u,v) in Ef} yy[d,p,u,v]<=1;*/
/*s.t. LinkMappingCon8{u in Nv}: sum{d in D,p in Path,(u,v) in Ef} yy[d,p,u,v]=1;*/

/*s.t. LinkMappingCon11{(u,v) in Ef}: sum{d in D,p in P[d]}yy[d,p,u,v]<=M[u,v];*/

/*s.t. LinkMappingCon12{d in D,p in Path}: sum{(u,v) in Ef} yy[d,p,u,v]=2*y[d,p];*/
/*s.t. LinkMappingCon12{d in D,p in Path}: sum{(u,v) in PathLinks[p]} yy[d,p,u,v]=2*z[d,p];*/

/*s.t. LinkMappingCon6{d in D}: sum{p in Path,(u,v) in Ef} yy[d,p,u,v]=1;*/
/*s.t. LinkMappingCon9{d1 in D}: sum{d in D,p in P[d1],(u,v) in Ef} yy[d,p,u,v]<=1;*/
/*s.t. LinkMappingCon2{u in Nv,(u,v) in A[u]}: sum{d in Du[u],p in Path} Sita[p,u,v]*yy[d,p,u,v]<=M[u,v]*PNum[u,v];*/

/*s.t. OpticalRelatedCon1{d in D}: B[d]=sum{p in Path} y[d,p]*NSd[d,p];*/
/*s.t. OpticalRelatedCon2{d in D}: E[d]=S[d]+B[d]-1;*/
/*s.t. OpticalRelatedCon5{d in D}: E[d]<=MSlots;*/
/*s.t. OpticalRelatedCon3{d in D}: S[d]=sum{p in P[d]}sum{i in FS[d,p]}fs[d,p,i]*x[d,p,i];*/
/*s.t. OpticalRelatedCon4{d in D,p in P[d]}:sum{i in FS[d,p]}x[d,p,i]=y[d,p];*/
/*s.t. OpticalRelatedCon6{di in D,dj in D}: Sigma[di,dj] + Sigma[dj,di] = 1;*/
/*s.t. OpticalRelatedCon7{di in D,dj in D,pa in P[di],pb in P[dj]}:E[dj]-S[di]<=(MSlots+1)*(Sigma[di,dj]+2-y[di,pa]);*/


data;
set MSet:=0 1 2 3 4 5 6 7 8 9;/*索引集合，{0,1,...,M-1}*/
set Path:=0 1 2 3 4 5 6;
set P[0]:=0 1 6;
set P[1]:=4 5;
set P[2]:=2 3;
/*路径p的集合*/

/*
set PathNf[0]:=0 1;
set PathNf[1]:=2 3;
set PathNf[2]:=2 3;
set PathNf[3]:=0 2;
set PathNf[4]:=1 3;
set PathNf[5]:=2 3;
PathNf{p in Path};路径上的f节点集合*/
/*
set PathNv[0]:=4 5;
set PathNv[1]:=4 5;
set PathNv[2]:=4 7;
set PathNv[3]:=4 7;
set PathNv[4]:=5 7;
set PathNv[5]:=5 7;
set PathNv{p in Path};路径上的虚拟节点集合*/
/*
set PathLinks[0]:=
4 0
0 4
5 1
1 5
0 6
6 0
6 1
1 6
;
set PathLinks[1]:=
4 2
2 4
5 3
3 5
2 6
6 2
3 6
6 3
;
set PathLinks[2]:=
4 2
2 4
7 3
3 7
2 6
6 2
3 6
6 3
;
set PathLinks[3]:=
4 0
0 4
7 2
2 7
2 6
6 2
0 6
6 0
;
set PathLinks[4]:=
5 1
1 5
7 3
3 7
3 6
6 3
1 6
6 1
;
set PathLinks[5]:=
5 3
3 5
7 2
2 7
3 6
6 3
2 6
6 2
;
set PathLinks[6]:=
4 2
2 4
5 1
1 5
2 6
6 2
1 6
6 1
;
*/
/*set PathLinks{p in Path};路径上的虚拟节点->f节点的链路集合*/


set Nv:=4 5 7;/*虚拟节点的集合*/
set F:=0 1 2 3;/*服务器节点（facility nodes）的集合*/
set Na:=0 1 2 3 4 5 6 7;/*辅助图的节点集合，Na=F并Nv并网络节点*/
set A[4]:=
4 0
4 2
;/*与节点（facility nodes）u的辅助边的集合*/

set A[5]:=
5 1
5 3
;/*与节点（facility nodes）u的辅助边的集合*/

set A[7]:=
7 2
7 3
;/*与节点（facility nodes）u的辅助边的集合*/

set Afa[0]:=
4 0
;/*与物理节点（facility nodes）u的辅助边的集合*/

set Afa[1]:=
5 1
;/*与物理节点（facility nodes）u的辅助边的集合*/

set Afa[2]:=
4 2
7 2
;/*与物理节点（facility nodes）u的辅助边的集合*/

set Afa[3]:=
5 3
7 3
;/*与物理节点（facility nodes）u的辅助边的集合*/

set Af[4]:=0 2;/*每个虚拟节点可映射的节点集合*/
set Af[5]:=1 3;/*每个虚拟节点可映射的节点集合*/
set Af[7]:=2 3;/*每个虚拟节点可映射的节点集合*/

set FS[0,0]:=0 1 2 3 4 5 6 7 8 9;/*路径p上可能的起始频谱槽索引集合*/
set FS[0,1]:=0 1 2 3 4 5 6 7 8 9;/*路径p上可能的起始频谱槽索引集合*/
set FS[0,2]:=0 1 2 3 4 5 6 7 8 9;/*路径p上可能的起始频谱槽索引集合*/
set FS[0,3]:=0 1 2 3 4 5 6 7 8 9;/*路径p上可能的起始频谱槽索引集合*/
set FS[0,4]:=0 1 2 3 4 5 6 7 8 9;/*路径p上可能的起始频谱槽索引集合*/
set FS[0,5]:=0 1 2 3 4 5 6 7 8 9;/*路径p上可能的起始频谱槽索引集合*/
set FS[0,6]:=6;/*路径p上可能的起始频谱槽索引集合*/
set FS[1,0]:=0 1 2 3 4 5 6 7 8 9;/*路径p上可能的起始频谱槽索引集合*/
set FS[1,1]:=0 1 2 3 4 5 6 7 8 9;/*路径p上可能的起始频谱槽索引集合*/
set FS[1,2]:=0 1 2 3 4 5 6 7 8 9;/*路径p上可能的起始频谱槽索引集合*/
set FS[1,3]:=0 1 2 3 4 5 6 7 8 9;/*路径p上可能的起始频谱槽索引集合*/
set FS[1,4]:=0 1 2 3 4 5 6 7 8 9;/*路径p上可能的起始频谱槽索引集合*/
set FS[1,5]:=0 1 2 3 4 5 6 7 8 9;/*路径p上可能的起始频谱槽索引集合*/
set FS[1,6]:=0 1 2 3 4 5 6 7 8 9;/*路径p上可能的起始频谱槽索引集合*/
set FS[2,0]:=0 1 2 3 4 5 6 7 8 9;/*路径p上可能的起始频谱槽索引集合*/
set FS[2,1]:=0 1 2 3 4 5 6 7 8 9;/*路径p上可能的起始频谱槽索引集合*/
set FS[2,2]:=0 1 2 3 4 5 6 7 8 9;/*路径p上可能的起始频谱槽索引集合*/
set FS[2,3]:=0 1 2 3 4 5 6 7 8 9;/*路径p上可能的起始频谱槽索引集合*/
set FS[2,4]:=0 1 2 3 4 5 6 7 8 9;/*路径p上可能的起始频谱槽索引集合*/
set FS[2,5]:=0 1 2 3 4 5 6 7 8 9;/*路径p上可能的起始频谱槽索引集合*/
set FS[2,6]:=0 1 2 3 4 5 6 7 8 9;/*路径p上可能的起始频谱槽索引集合*/

set Ef:=
4 0
4 2
5 1
5 3
7 2
7 3
;/*与服务器节点的辅助边的集合*/

set D:=0 1 2;/*虚拟链路集合*/
set DNo[0]:=1 2;/*虚拟链路集合*/
set DNo[1]:=0 2;/*虚拟链路集合*/
set DNo[2]:=0 1;/*虚拟链路集合*/

set Du[4]:=0 2;
set Du[5]:=0 1;
set Du[7]:=1 2;
/*Du{u in Nv};具有虚拟节点u的虚拟链路集合*/

param NSd:=
0 0 1
0 1 2
0 2 3
0 3 1
0 4 2
0 5 1
0 6 4
1 0 3
1 1 3
1 2 3
1 3 3
1 4 3
1 5 3
1 6 3
2 0 3
2 1 3
2 2 3
2 3 3
2 4 3
2 5 3
2 6 3
;/*虚拟网络在路径p上请求的频谱槽数量*/
/*param NSd[1]:=2;虚拟网络在路径p上请求的频谱槽数量*/
param H:=
0 4
1 4
2 4
3 4
4 4
5 4
6 4
;/*路径p的跳数*/
/*param H[1]:=3;路径p的跳数*/
param PNum:=
4 0 2
0 4 2
4 2 3
2 4 3
5 1 3
1 5 3
5 3 2
3 5 2
7 2 2
2 7 2
7 3 2
3 7 2
0 6 2
6 0 2
1 6 3
6 1 3
2 6 5
6 2 5
3 6 3
6 3 3
;/*经过链路(u,v)的路径数量*/

param Sita:=
0 4 0 1
0 4 2 0
0 5 1 1
0 5 3 0
0 7 2 0
0 7 3 0
1 4 0 0
1 4 2 1
1 5 1 0
1 5 3 1
1 7 2 0
1 7 3 0
2 4 0 0
2 4 2 1
2 5 1 0
2 5 3 0
2 7 2 0
2 7 3 1
3 4 0 1
3 4 2 0
3 5 1 0
3 5 3 0
3 7 2 1
3 7 3 0
4 4 0 0
4 4 2 0
4 5 1 1
4 5 3 0
4 7 2 0
4 7 3 1
5 4 0 0
5 4 2 0
5 5 1 0
5 5 3 1
5 7 2 1
5 7 3 0
6 4 0 0
6 4 2 1
6 5 1 1
6 5 3 0
6 7 2 0
6 7 3 0
;
/*param Sita{p in P,(u,v) in Ef}, binary;二进制变量，*/

/*param fs{d in D,p in P,i in MSet};路径p上的第i个起始频谱槽索引*/
param fs:=
0 0 0 -1
0 0 1 1
0 0 2 2
0 0 3 3
0 0 4 4
0 0 5 5
0 0 6 6
0 0 7 7
0 0 8 8
0 0 9 9
0 1 0 2
0 1 1 2
0 1 2 2
0 1 3 3
0 1 4 4
0 1 5 5
0 1 6 6
0 1 7 7
0 1 8 8
0 1 9 9
0 2 0 0
0 2 1 1
0 2 2 2
0 2 3 3
0 2 4 4
0 2 5 5
0 2 6 6
0 2 7 7
0 2 8 8
0 2 9 9
0 3 0 0
0 3 1 1
0 3 2 2
0 3 3 3
0 3 4 4
0 3 5 5
0 3 6 6
0 3 7 7
0 3 8 8
0 3 9 9
0 4 0 0
0 4 1 1
0 4 2 2
0 4 3 3
0 4 4 4
0 4 5 5
0 4 6 6
0 4 7 7
0 4 8 8
0 4 9 9
0 5 0 0
0 5 1 1
0 5 2 2
0 5 3 3
0 5 4 4
0 5 5 5
0 5 6 6
0 5 7 7
0 5 8 8
0 5 9 9
0 6 0 6
0 6 1 6
0 6 2 6
0 6 3 6
0 6 4 6
0 6 5 6
0 6 6 6
0 6 7 7
0 6 8 8
0 6 9 9
1 0 0 0
1 0 1 1
1 0 2 2
1 0 3 3
1 0 4 4
1 0 5 5
1 0 6 6
1 0 7 7
1 0 8 8
1 0 9 9
1 1 0 2
1 1 1 2
1 1 2 2
1 1 3 3
1 1 4 4
1 1 5 5
1 1 6 6
1 1 7 7
1 1 8 8
1 1 9 9
1 2 0 0
1 2 1 1
1 2 2 2
1 2 3 3
1 2 4 4
1 2 5 5
1 2 6 6
1 2 7 7
1 2 8 8
1 2 9 9
1 3 0 0
1 3 1 1
1 3 2 2
1 3 3 3
1 3 4 4
1 3 5 5
1 3 6 6
1 3 7 7
1 3 8 8
1 3 9 9
1 4 0 0
1 4 1 1
1 4 2 2
1 4 3 3
1 4 4 4
1 4 5 5
1 4 6 6
1 4 7 7
1 4 8 8
1 4 9 9
1 5 0 0
1 5 1 1
1 5 2 2
1 5 3 3
1 5 4 4
1 5 5 5
1 5 6 6
1 5 7 7
1 5 8 8
1 5 9 9
1 6 0 0
1 6 1 1
1 6 2 2
1 6 3 3
1 6 4 4
1 6 5 5
1 6 6 6
1 6 7 7
1 6 8 8
1 6 9 9
2 0 0 0
2 0 1 1
2 0 2 2
2 0 3 3
2 0 4 4
2 0 5 5
2 0 6 6
2 0 7 7
2 0 8 8
2 0 9 9
2 1 0 2
2 1 1 2
2 1 2 2
2 1 3 3
2 1 4 4
2 1 5 5
2 1 6 6
2 1 7 7
2 1 8 8
2 1 9 9
2 2 0 0
2 2 1 1
2 2 2 2
2 2 3 3
2 2 4 4
2 2 5 5
2 2 6 6
2 2 7 7
2 2 8 8
2 2 9 9
2 3 0 0
2 3 1 1
2 3 2 2
2 3 3 3
2 3 4 4
2 3 5 5
2 3 6 6
2 3 7 7
2 3 8 8
2 3 9 9
2 4 0 0
2 4 1 1
2 4 2 2
2 4 3 3
2 4 4 4
2 4 5 5
2 4 6 6
2 4 7 7
2 4 8 8
2 4 9 9
2 5 0 0
2 5 1 1
2 5 2 2
2 5 3 3
2 5 4 4
2 5 5 5
2 5 6 6
2 5 7 7
2 5 8 8
2 5 9 9
2 6 0 0
2 6 1 1
2 6 2 2
2 6 3 3
2 6 4 4
2 6 5 5
2 6 6 6
2 6 7 7
2 6 8 8
2 6 9 9
;

param Degree:=
4 2
5 2
7 2
;
/*Degree{u in Nv};虚拟节点的度*/

param MSlots:=9;/*最大的频谱槽索引*/

/*
set path[0]:=
4 0
0 6
6 1
1 5
;

set path[1]:=
4 2
2 6
6 3
3 5
;

set path[2]:=
4 2
2 6
6 3
3 7
;

set path[3]:=
4 0
0 6
6 2
2 7
;

set path[4]:=
5 1
1 6
6 3
3 7
;

set path[5]:=
5 3
3 6
6 2
2 7
;

set path[6]:=
5 1
1 6
6 2
2 4
;
*/

end;