set MSet;/*索引集合，{0,1,...,M-1}*/
set P;/*路径p的集合*/
set Nv;/*虚拟节点的集合*/
set F;/*服务器节点（facility nodes）的集合*/
set Na;/*辅助图的节点集合，Na=F并Nv并网络节点*/
set A{u in Nv}, within Na cross Na;/*与虚拟节点u的辅助边的集合*/
set Afa{u in F}, within Na cross Na;/*与物理节点（facility nodes）u的辅助边的集合*/
/*set ASet{u in F};与节点（facility nodes）u的辅助边的集合*/
set FS{p in P};/*路径p上可能的起始频谱槽索引集合*/
set Ef, within Na cross Na;/*与服务器节点的辅助边的集合*/
set Af{u in Nv};/*每个虚拟节点可映射的节点集合*/

param NSd{p in P};/*虚拟网络在路径p上请求的频谱槽数量*/
param H{p in P};/*路径p的跳数*/
param PNum{(u,v) in Ef};/*经过链路(u,v)的路径数量*/
param fs{p in P,i in MSet};/*路径p上的第i个起始频谱槽索引*/
param MSlots;/*最大的频谱槽索引*/
param Sita{p in P,(u,v) in Ef}, binary;/*二进制变量，*/

var y{p in P}, binary;/*二进制变量，如果虚拟网络映射在路径p上，则为1，否则为0*/
var M{u in Nv,v in Af[u]}, binary;/*二进制变量，*/
var E;/*分配给虚拟网洛的频谱槽终点索引*/
var S;/*分配给虚拟网洛的频谱槽起始节点索引*/
var B;/*为虚拟网络分配的频谱槽总的数量*/
var x{p in P,i in FS[p]}, binary;

minimize slotsMin: sum{p in P} NSd[p]*y[p]*(H[p]-2);

s.t. NodeAssignmentCon1{u in Nv}: sum{(u,v) in A[u]} M[u,v]=1;
s.t. NodeAssignmentCon2{v in F}: sum{(u,v) in Afa[v]} M[u,v]<=1;

s.t. LinkMappingCon1: sum{p in P} y[p]=1;
s.t. LinkMappingCon2{u in Nv,(u,v) in A[u]}: sum{p in P} Sita[p,u,v]*y[p]<=M[u,v]*PNum[u,v];

s.t. OpticalRelatedCon1: sum{p in P} y[p]*NSd[p]=B;
s.t. OpticalRelatedCon2: E=S+B-1;
s.t. OpticalRelatedCon5: E<=MSlots;
s.t. OpticalRelatedCon3: S=sum{p in P}sum{i in FS[p]}fs[p,i]*x[p,i];
s.t. OpticalRelatedCon4{p in P}:sum{i in FS[p]}x[p,i]=y[p];

data;
set MSet:=0 1 2 3 4 5 6 7 8 9;/*索引集合，{0,1,...,M-1}*/
set P:=0 1;/*路径p的集合*/
set Nv:=4 5;/*虚拟节点的集合*/
set F:=0 1 2 3;/*服务器节点（facility nodes）的集合*/
set Na:=0 1 2 3 4 5 6;/*辅助图的节点集合，Na=F并Nv并网络节点*/
set A[4]:=
4 0
4 2
;/*与节点（facility nodes）u的辅助边的集合*/

set A[5]:=
5 1
5 3
;/*与节点（facility nodes）u的辅助边的集合*/

set Afa[0]:=
4 0
;/*与物理节点（facility nodes）u的辅助边的集合*/

set Afa[1]:=
5 1
;/*与物理节点（facility nodes）u的辅助边的集合*/

set Afa[2]:=
4 2
;/*与物理节点（facility nodes）u的辅助边的集合*/

set Afa[3]:=
5 3
;/*与物理节点（facility nodes）u的辅助边的集合*/

set Af[4]:=0 2;/*每个虚拟节点可映射的节点集合*/
set Af[5]:=1 3;/*每个虚拟节点可映射的节点集合*/

set FS[0]:=0 1 2 3 4 5 6 7 8 9;/*路径p上可能的起始频谱槽索引集合*/
set FS[1]:=0 1 2 3 4 5 6 7 8 9;/*路径p上可能的起始频谱槽索引集合*/

set Ef:=
4 0
4 2
5 1
5 3
;

param NSd
0 4
1 2
;/*虚拟网络在路径p上请求的频谱槽数量*/
/*param NSd[1]:=2;虚拟网络在路径p上请求的频谱槽数量*/
param H
0 4
1 4
;/*路径p的跳数*/
/*param H[1]:=3;路径p的跳数*/
param PNum:=
4 0 1
4 2 1
5 1 1
5 3 1
;/*经过链路(u,v)的路径数量*/

param Sita:=
0 4 0 1
0 4 2 0
1 4 0 0
1 4 2 1
0 5 1 1
0 5 3 0
1 5 1 0
1 5 3 1
;
/*param Sita{p in P,(u,v) in Ef}, binary;二进制变量，*/

/*param fs{p in P,i in MSet};路径p上的第i个起始频谱槽索引*/
param fs:=
0 0 0
0 1 1
0 2 2
0 3 3
0 4 4
0 5 5
0 6 6
0 7 7
0 8 8
0 9 9
1 0 2
1 1 2
1 2 2
1 3 3
1 4 4
1 5 5
1 6 6
1 7 7
1 8 8
1 9 9
;


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
*/

end;