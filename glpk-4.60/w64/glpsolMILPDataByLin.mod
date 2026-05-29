set Nv;/*虚拟节点的集合*/
set Lv, within Nv cross Nv;/*虚拟链路的集合*/
set Ns;/*物理节点的集合*/
set Nss{k in Ns};/*物理节点k对应的链路的另外一个节点集合*/
set Nss1{k in Ns};/*物理节点k对应的链路的前驱节点集合*/
set Ls, within Ns cross Ns;/*物理链路集合*/
set X{i in Nv};/*每个虚拟节点可映射的物理节点集合*/
set SlotIndex;/*频谱槽索引集合*/
set MD;/*调制模式，MD=1,2,3,4,6,8,分别对应BPSK,QPSK,8QAM,16QAM,64QAM,256QAM*/
set Slot{(i,j) in Lv,a in SlotIndex,model in MD};/*从a到a+1,...,a+|bw/model|+G-1*/
set Slot1{(i,j) in Lv,a in SlotIndex,model in MD};/*从a+|bw/model|+G-1到a+|bw/model|+G-1+1,...,a*/

param c{k in Ns};/*物理节点的计算单位代价*/
param cs{(i,j) in Ls};/*物理链路的频谱槽单位代价，与长度成比例*/
param p{i in Nv};/*虚拟节点CPU*/
param pbw{(i,j) in Lv};/*虚拟链路带宽请求*/
param F;/*最大的频谱槽索引*/
param U{k in Ns};/*物理节点k的剩余cpu*/
param USlot{(i,j) in Ls,t in SlotIndex};/*物理链路频谱槽的状态，1表示空闲；0表示被占用*/
param d{(i,j) in Ls};/*物理链路的长度*/
param dis{m in MD};/*调制模式最大传输距离*/
param R;/*最大的整数*/
param G;/*隔离槽数量*/


var Z{(i,j) in Lv,(m,n) in Ls,a in SlotIndex,md in MD},binary;/*虚拟链路映射到物理链路上，其起始频谱槽索引为a，调制模式为mod*/
var A{i in Nv,k in Ns},binary;/*虚拟节点i映射到物理节点k上，如果成功则为1，否则为0*/
var M{(i,j) in Lv,(m,n) in Ls,a in SlotIndex}, binary;/*二进制变量，如果虚拟网络链路(i,j)映射在链路(m,n)的起始索引a上，则为1，否则为0*/
var T{(i,j) in Lv,a in SlotIndex},binary;/*虚拟链路(i,j)映射到物理网络中的起始索引a上*/
var S{(i,j) in Lv,m in MD},binary;/*虚拟链路(i,j)选择的调制模式mod*/

minimize slotsMin: sum{(i,j) in Lv}sum{(m,n) in Ls}sum{a in SlotIndex}sum{md in MD}cs[m,n]*((pbw[i,j]/(12.5*md)+1)+G)*Z[i,j,m,n,a,md]+sum{i in Nv}sum{k in Ns}c[k]*p[i]*A[i,k];
/*公式4*/
s.t. NodeAssignmentCon1{i in Nv}: sum{k in X[i]} A[i,k]=1;/*公式5*/
s.t. NodeAssignmentCon2{k in Ns}: sum{i in Nv} A[i,k]<=1;/*公式6*/
s.t. VLinkAssignmentCon1{(i,j) in Lv,k in Ns}: sum{a in SlotIndex}sum{m in Nss1[k]}M[i,j,m,k,a]-sum{a in SlotIndex}sum{n in Nss[k]}M[i,j,k,n,a]=A[j,k]-A[i,k];/*公式7*/
s.t. SpectrumAssignmentCon1{(i,j) in Lv}: sum{a in SlotIndex} T[i,j,a]=1;/*公式8*/
s.t. SpectrumAssignmentCon2{(i,j) in Lv,a in SlotIndex}: sum{(m,n) in Ls} M[i,j,m,n,a]>=T[i,j,a];/*公式9*/
s.t. SpectrumAssignmentCon3{(i,j) in Lv,a in SlotIndex}: sum{(m,n) in Ls} M[i,j,m,n,a]<=R*T[i,j,a];/*公式10*/
s.t. ModulationAssignmentCon1{(i,j) in Lv}: sum{md in MD} S[i,j,md]=1;/*公式11*/
s.t. ModulationAssignmentCon2{(i,j) in Lv}: (sum{(m,n) in Ls}sum{a in SlotIndex}(M[i,j,m,n,a]*d[m,n]))<=(sum{md in MD}(S[i,j,md]*dis[md]));/*公式12*/
s.t. NetResAssignmentCon1{k in Ns}: sum{i in Nv}p[i]*A[i,k]<=U[k];/*公式13/
s.t. NetResAssignmentCon2{(i,j) in Lv,(m,n) in Ls,a in SlotIndex,md in MD}: (pbw[i,j]/(12.5*md)+G)*Z[i,j,m,n,a,md]<=sum{t in Slot[i,j,a,md]}U[m,n,t];/*公式14*/
s.t. NetResAssignmentCon3{(m,n) in Ls,a in SlotIndex}: sum{(i,j) in Lv}sum{md in MD}sum{t in Slot1[i,j,a,md]}Z[i,j,m,n,t,md]<=USlot[m,n,a];/*公式15*/
s.t. NonlinearCon1{(i,j) in Lv,(m,n) in Ls,a in SlotIndex,md in MD}: M[i,j,m,n,a]+S[i,j,md]-1<=Z[i,j,m,n,a,md];/*公式16*/
s.t. NonlinearCon2{(i,j) in Lv,(m,n) in Ls,a in SlotIndex,md in MD}: M[i,j,m,n,a]+S[i,j,md]>=2*Z[i,j,m,n,a,md];/*公式17*/
s.t. cxhCon2{(i,j) in Lv,(m,n) in Ls,a in SlotIndex,md in MD}: a+(pbw[i,j]/(12.5*md)+G)*Z[i,j,m,n,a,md]<=G-1;/*公式14cxh补充2020-1-29*/


data;
set Nv:=0 1;/*虚拟节点集合*/
set Lv:=
0 1
;
/*虚拟链路集合*/

set Ns:=0 1 2 3 4 5 ;/*物理节点集合*/
set Nss[0]:=1 2 ;
set Nss[1]:=3 4 ;
set Nss[2]:=4 ;
set Nss[3]:=4 5 ;
set Nss[4]:=5 ;
set Nss[5]:=;
set Nss1[0]:=;
set Nss1[1]:=0 ;
set Nss1[2]:=0 ;
set Nss1[3]:=1 ;
set Nss1[4]:=1 2 3 ;
set Nss1[5]:=3 4 ;

set X[0]:=3 4 ;
set X[1]:=3 ;
set SlotIndex:=0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 ;
set MD:=1,2,3,4,6,8;

set Slot[0,1,0,1]:=0 1 2 3 4 5 6 ;
set Slot[0,1,0,2]:=0 1 2 3 ;
set Slot[0,1,0,3]:=0 1 2 ;
set Slot[0,1,0,4]:=0 1 ;
set Slot[0,1,0,6]:=0 1 ;
set Slot[0,1,0,8]:=0 ;
set Slot[0,1,1,1]:=1 2 3 4 5 6 7 ;
set Slot[0,1,1,2]:=1 2 3 4 ;
set Slot[0,1,1,3]:=1 2 3 ;
set Slot[0,1,1,4]:=1 2 ;
set Slot[0,1,1,6]:=1 2 ;
set Slot[0,1,1,8]:=1 ;
set Slot[0,1,2,1]:=2 3 4 5 6 7 8 ;
set Slot[0,1,2,2]:=2 3 4 5 ;
set Slot[0,1,2,3]:=2 3 4 ;
set Slot[0,1,2,4]:=2 3 ;
set Slot[0,1,2,6]:=2 3 ;
set Slot[0,1,2,8]:=2 ;
set Slot[0,1,3,1]:=3 4 5 6 7 8 9 ;
set Slot[0,1,3,2]:=3 4 5 6 ;
set Slot[0,1,3,3]:=3 4 5 ;
set Slot[0,1,3,4]:=3 4 ;
set Slot[0,1,3,6]:=3 4 ;
set Slot[0,1,3,8]:=3 ;
set Slot[0,1,4,1]:=4 5 6 7 8 9 10 ;
set Slot[0,1,4,2]:=4 5 6 7 ;
set Slot[0,1,4,3]:=4 5 6 ;
set Slot[0,1,4,4]:=4 5 ;
set Slot[0,1,4,6]:=4 5 ;
set Slot[0,1,4,8]:=4 ;
set Slot[0,1,5,1]:=5 6 7 8 9 10 11 ;
set Slot[0,1,5,2]:=5 6 7 8 ;
set Slot[0,1,5,3]:=5 6 7 ;
set Slot[0,1,5,4]:=5 6 ;
set Slot[0,1,5,6]:=5 6 ;
set Slot[0,1,5,8]:=5 ;
set Slot[0,1,6,1]:=6 7 8 9 10 11 12 ;
set Slot[0,1,6,2]:=6 7 8 9 ;
set Slot[0,1,6,3]:=6 7 8 ;
set Slot[0,1,6,4]:=6 7 ;
set Slot[0,1,6,6]:=6 7 ;
set Slot[0,1,6,8]:=6 ;
set Slot[0,1,7,1]:=7 8 9 10 11 12 13 ;
set Slot[0,1,7,2]:=7 8 9 10 ;
set Slot[0,1,7,3]:=7 8 9 ;
set Slot[0,1,7,4]:=7 8 ;
set Slot[0,1,7,6]:=7 8 ;
set Slot[0,1,7,8]:=7 ;
set Slot[0,1,8,1]:=8 9 10 11 12 13 14 ;
set Slot[0,1,8,2]:=8 9 10 11 ;
set Slot[0,1,8,3]:=8 9 10 ;
set Slot[0,1,8,4]:=8 9 ;
set Slot[0,1,8,6]:=8 9 ;
set Slot[0,1,8,8]:=8 ;
set Slot[0,1,9,1]:=9 10 11 12 13 14 15 ;
set Slot[0,1,9,2]:=9 10 11 12 ;
set Slot[0,1,9,3]:=9 10 11 ;
set Slot[0,1,9,4]:=9 10 ;
set Slot[0,1,9,6]:=9 10 ;
set Slot[0,1,9,8]:=9 ;
set Slot[0,1,10,1]:=10 11 12 13 14 15 16 ;
set Slot[0,1,10,2]:=10 11 12 13 ;
set Slot[0,1,10,3]:=10 11 12 ;
set Slot[0,1,10,4]:=10 11 ;
set Slot[0,1,10,6]:=10 11 ;
set Slot[0,1,10,8]:=10 ;
set Slot[0,1,11,1]:=11 12 13 14 15 16 17 ;
set Slot[0,1,11,2]:=11 12 13 14 ;
set Slot[0,1,11,3]:=11 12 13 ;
set Slot[0,1,11,4]:=11 12 ;
set Slot[0,1,11,6]:=11 12 ;
set Slot[0,1,11,8]:=11 ;
set Slot[0,1,12,1]:=12 13 14 15 16 17 18 ;
set Slot[0,1,12,2]:=12 13 14 15 ;
set Slot[0,1,12,3]:=12 13 14 ;
set Slot[0,1,12,4]:=12 13 ;
set Slot[0,1,12,6]:=12 13 ;
set Slot[0,1,12,8]:=12 ;
set Slot[0,1,13,1]:=13 14 15 16 17 18 19 ;
set Slot[0,1,13,2]:=13 14 15 16 ;
set Slot[0,1,13,3]:=13 14 15 ;
set Slot[0,1,13,4]:=13 14 ;
set Slot[0,1,13,6]:=13 14 ;
set Slot[0,1,13,8]:=13 ;
set Slot[0,1,14,1]:=14 15 16 17 18 19 ;
set Slot[0,1,14,2]:=14 15 16 17 ;
set Slot[0,1,14,3]:=14 15 16 ;
set Slot[0,1,14,4]:=14 15 ;
set Slot[0,1,14,6]:=14 15 ;
set Slot[0,1,14,8]:=14 ;
set Slot[0,1,15,1]:=15 16 17 18 19 ;
set Slot[0,1,15,2]:=15 16 17 18 ;
set Slot[0,1,15,3]:=15 16 17 ;
set Slot[0,1,15,4]:=15 16 ;
set Slot[0,1,15,6]:=15 16 ;
set Slot[0,1,15,8]:=15 ;
set Slot[0,1,16,1]:=16 17 18 19 ;
set Slot[0,1,16,2]:=16 17 18 19 ;
set Slot[0,1,16,3]:=16 17 18 ;
set Slot[0,1,16,4]:=16 17 ;
set Slot[0,1,16,6]:=16 17 ;
set Slot[0,1,16,8]:=16 ;
set Slot[0,1,17,1]:=17 18 19 ;
set Slot[0,1,17,2]:=17 18 19 ;
set Slot[0,1,17,3]:=17 18 19 ;
set Slot[0,1,17,4]:=17 18 ;
set Slot[0,1,17,6]:=17 18 ;
set Slot[0,1,17,8]:=17 ;
set Slot[0,1,18,1]:=18 19 ;
set Slot[0,1,18,2]:=18 19 ;
set Slot[0,1,18,3]:=18 19 ;
set Slot[0,1,18,4]:=18 19 ;
set Slot[0,1,18,6]:=18 19 ;
set Slot[0,1,18,8]:=18 ;
set Slot[0,1,19,1]:=19 ;
set Slot[0,1,19,2]:=19 ;
set Slot[0,1,19,3]:=19 ;
set Slot[0,1,19,4]:=19 ;
set Slot[0,1,19,6]:=19 ;
set Slot[0,1,19,8]:=19 ;

req[98].cpu=
0:13.071652499999999 1:24.339965 
req[98].link.bw=
0:51.94051 

0=8.965065000000003 1=6.054664999999995 2=7.996897499999996 3=48.5882675 4=21.254710000000003 5=6.711307500000011 
rest cpu[]:0=8.965065000000003 1=6.054664999999995 2=7.996897499999996 3=48.5882675 4=21.254710000000003 5=6.711307500000011 
2020/02/11 19:54:41sub.slots:
0-1sub.slots:
1 1 1 1 0 0 0 0 0 1 1 1 0 0 0 0 0 0 0 1 .
0-2sub.slots:
1 1 0 0 0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 .
1-3sub.slots:
1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 0 1 .
1-4sub.slots:
1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 .
2-4sub.slots:
1 1 1 1 1 1 0 0 1 1 1 1 1 1 1 1 1 1 0 0 .
3-4sub.slots:
1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 .
3-5sub.slots:
1 0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 .
4-5sub.slots:
1 1 1 1 1 1 0 0 0 0 1 1 1 1 1 1 1 0 0 0 .



data;
set Nv:=0 1 2;/*虚拟节点集合*/
set Lv:=
0 1
0 2
;
/*虚拟链路集合*/

set Ns:=0 1 2 3 4;/*物理节点集合*/
set Nss[0]:=1 2 3;
set Nss[1]:=3;
set Nss[2]:=3 4;
set Nss[3]:=4;
set Nss[4]:=;

set Nss1[0]:=;
set Nss1[1]:=0;
set Nss1[2]:=0;
set Nss1[3]:=0 1 2;
set Nss1[4]:=2 3;

set Ls:=
0 1
0 2
0 3
1 3
2 3
2 4
3 4
;
/*物理链路集合*/
set X[0]:=0 1 2 3 4;/*虚拟节点0可映射的物理节点*/
set X[1]:=0 2 3 4;
set X[2]:=0 2 4;

set SlotIndex:=0 1 2 3 4 5 6;/*频谱槽索引集合*/

set MD:=1 2;/*调制模式，MD=1,2,3,4,6,8,分别对应BPSK,QPSK,8QAM,16QAM,64QAM,256QAM*/

  
set Slot[0,1,0,1]:=0 1 2 3 4 5;
set Slot[0,1,0,2]:=0 1 2 3;
set Slot[0,1,1,1]:=1 2 3 4 5 6;
set Slot[0,1,1,2]:=1 2 3 4;
set Slot[0,1,2,1]:=2 3 4 5 6;
set Slot[0,1,2,2]:=2 3 4 5;
set Slot[0,1,3,1]:=3 4 5 6;
set Slot[0,1,3,2]:=3 4 5 6;
set Slot[0,1,4,1]:=4 5 6;
set Slot[0,1,4,2]:=4 5 6;
set Slot[0,1,5,1]:=5 6;
set Slot[0,1,5,2]:=5 6;
set Slot[0,1,6,1]:=6;
set Slot[0,1,6,2]:=6;

set Slot[0,2,0,1]:=0 1 2 3;
set Slot[0,2,0,2]:=0 1 2;
set Slot[0,2,1,1]:=1 2 3 4;
set Slot[0,2,1,2]:=1 2 3;
set Slot[0,2,2,1]:=2 3 4 5;
set Slot[0,2,2,2]:=2 3 4;
set Slot[0,2,3,1]:=3 4 5 6;
set Slot[0,2,3,2]:=3 4 5;
set Slot[0,2,4,1]:=4 5 6;
set Slot[0,2,4,2]:=4 5 6;
set Slot[0,2,5,1]:=5 6;
set Slot[0,2,5,2]:=5 6;
set Slot[0,2,6,1]:=6;
set Slot[0,2,6,2]:=6;

set Slot1[0,1,0,1]:=0;
set Slot1[0,1,0,2]:=0;
set Slot1[0,1,1,1]:=0 1;
set Slot1[0,1,1,2]:=0 1;
set Slot1[0,1,2,1]:=0 1 2;
set Slot1[0,1,2,2]:=0 1 2;
set Slot1[0,1,3,1]:=0 1 2 3;
set Slot1[0,1,3,2]:=0 1 2 3;
set Slot1[0,1,4,1]:=0 1 2 3 4;
set Slot1[0,1,4,2]:=1 2 3 4;
set Slot1[0,1,5,1]:=0 1 2 3 4 5;
set Slot1[0,1,5,2]:=2 3 4 5;
set Slot1[0,1,6,1]:=1 2 3 4 5 6;
set Slot1[0,1,6,2]:=3 4 5 6;

set Slot1[0,2,0,1]:=0;
set Slot1[0,2,0,2]:=0;
set Slot1[0,2,1,1]:=0 1;
set Slot1[0,2,1,2]:=0 1;
set Slot1[0,2,2,1]:=0 1 2;
set Slot1[0,2,2,2]:=0 1 2;
set Slot1[0,2,3,1]:=0 1 2 3;
set Slot1[0,2,3,2]:=1 2 3;
set Slot1[0,2,4,1]:=1 2 3 4;
set Slot1[0,2,4,2]:=2 3 4;
set Slot1[0,2,5,1]:=2 3 4 5;
set Slot1[0,2,5,2]:=3 4 5;
set Slot1[0,2,6,1]:=3 4 5 6;
set Slot1[0,2,6,2]:=4 5 6;

param c:=
0 50
1 20
2 40
3 30
4 60
;/*物理节点的计算单位代价*/
param cs:=
0 1 400
0 2 600
0 3 450
1 3 300
2 3 700
2 4 500
3 4 600
;/*物理链路的频谱槽单位代价，与长度成比例*/
param p:=
0 20
1 30
2 40
;/*虚拟节点CPU*/
param pbw:=
0 1 5
0 2 3
;/*虚拟链路带宽请求*/
param F:=4;/*最大的频谱槽索引*/
param U:=
0 50
1 20
2 40
3 30
4 60
;/*物理节点k的剩余cpu*/
param USlot:=
0 1 0 1
0 1 1 1
0 1 2 1
0 1 3 1
0 1 4 1
0 1 5 1
0 1 6 1
0 2 0 1
0 2 1 1
0 2 2 1
0 2 3 1
0 2 4 1
0 2 5 1
0 2 6 1
0 3 0 1
0 3 1 1
0 3 2 1
0 3 3 1
0 3 4 1
0 3 5 1
0 3 6 1
1 3 0 1
1 3 1 1
1 3 2 1
1 3 3 1
1 3 4 1
1 3 5 1
1 3 6 1
2 3 0 1
2 3 1 1
2 3 2 1
2 3 3 1
2 3 4 1
2 3 5 1
2 3 6 1
2 4 0 1
2 4 1 1
2 4 2 1
2 4 3 1
2 4 4 1
2 4 5 1
2 4 6 1
3 4 0 1
3 4 1 1
3 4 2 1
3 4 3 1
3 4 4 1
3 4 5 1
3 4 6 1
;/*物理链路频谱槽的状态，1表示空闲；0表示被占用*/
param d:=
0 1 400
0 2 600
0 3 450
1 3 300
2 3 700
2 4 500
3 4 600
;/*物理链路的长度*/
param dis:=
1 1000
2 2000
;/*调制模式最大传输距离*/
param R:=100000000;/*最大的整数*/
param G:=1;/*隔离槽数量*/

end;