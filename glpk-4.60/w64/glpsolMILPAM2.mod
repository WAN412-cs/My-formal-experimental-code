/* ILP模型用于虚拟光网络的链路映射 */

        set Nv; /* 虚拟节点的集合 */
        set Lv, within Nv cross Nv; /* 虚拟链路的集合 */
        set Ns; /* 物理节点的集合 */
        set Nss{k in Ns};/*物理节点k对应的链路的另外一个节点集合*/
        set Nss1{k in Ns};/*物理节点k对应的链路的前驱节点集合*/
        set Ls, within Ns cross Ns; /* 物理链路集合 */
        set SlotIndex; /* 频谱槽索引集合 */
        set MD; /* 调制模式，MD=1,2,3,4,6,8,分别对应BPSK,QPSK,8QAM,16QAM,64QAM,256QAM */
        set Slot{(i,j) in Lv, a in SlotIndex, model in MD}; /* 从a到a+1,...,a+|bw/model|+G-1 */
        set Slot1{(i,j) in Lv, a in SlotIndex, model in MD}; /* 从a+|bw/model|+G-1到a+|bw/model|+G-1+1,...,a */

        param cs{(i,j) in Ls}; /* 物理链路的频谱槽单位代价，与长度成比例 */
        param pbw{(i,j) in Lv}; /* 虚拟链路带宽请求 */
        param F; /* 最大的频谱槽索引 */
        param USlot{(i,j) in Ls, t in SlotIndex}; /* 物理链路频谱槽的状态，1表示空闲；0表示被占用 */
        param d{(i,j) in Ls}; /* 物理链路的长度 */
        param dis{m in MD}; /* 调制模式最大传输距离 */
        param R; /* 最大的整数 */
        param G; /* 隔离槽数量 */
        param A{i in Nv, k in Ns}, >= 0, default 0;


        var M{(i,j) in Lv, (m,n) in Ls, a in SlotIndex}, binary; /* 若虚拟链路(i,j)映射在物理链路(m,n)上且频谱槽起始位置为a，则为1 */
        var T{(i,j) in Lv, a in SlotIndex}, binary; /* 虚拟链路(i,j)映射到物理网络中的起始索引a上 */
        var S{(i,j) in Lv, m in MD}, binary; /* 虚拟链路(i,j)选择的调制模式mod */
        var Z{(i,j) in Lv, (m,n) in Ls, a in SlotIndex, md in MD}, binary; /* 虚拟链路映射到物理链路上，其起始频谱槽索引为a，调制模式为mod */

        /* 目标函数 */
        minimize linkMappingCost: sum{(i,j) in Lv} sum{(m,n) in Ls} sum{a in SlotIndex} sum{md in MD} cs[m,n] * ((pbw[i,j]/(12.5*md)+1)+G) * Z[i,j,m,n,a,md];

        /* 约束条件 */
        s.t. SpectrumAssignmentCon1{(i,j) in Lv}: sum{a in SlotIndex} T[i,j,a] = 1;
        s.t. SpectrumAssignmentCon2{(i,j) in Lv, a in SlotIndex}: sum{(m,n) in Ls} M[i,j,m,n,a] >= T[i,j,a];
        s.t. SpectrumAssignmentCon3{(i,j) in Lv, a in SlotIndex}: sum{(m,n) in Ls} M[i,j,m,n,a] <= R * T[i,j,a];
        s.t. ModulationAssignmentCon1{(i,j) in Lv}: sum{md in MD} S[i,j,md] = 1;
        s.t. ModulationAssignmentCon2{(i,j) in Lv}: (sum{(m,n) in Ls} sum{a in SlotIndex} (M[i,j,m,n,a] * d[m,n])) <= (sum{md in MD} (S[i,j,md] * dis[md]));
        s.t. NetResAssignmentCon2{(i,j) in Lv, (m,n) in Ls, a in SlotIndex, md in MD}: (pbw[i,j]/(12.5*md)+G) * Z[i,j,m,n,a,md] <= sum{t in Slot[i,j,a,md]} USlot[m,n,t];
        s.t. NetResAssignmentCon3{(m,n) in Ls, a in SlotIndex}: sum{(i,j) in Lv} sum{md in MD} sum{t in Slot1[i,j,a,md]} Z[i,j,m,n,t,md] <= USlot[m,n,a];
        /* 添加额外的链路映射约束，确保映射与节点映射结果一致 */
        s.t. NodeLinkMappingCon{(i,j) in Lv, (m,n) in Ls, a in SlotIndex}:
        M[i, j, m, n, a] <= A[i, m] * A[j, n];

        end;