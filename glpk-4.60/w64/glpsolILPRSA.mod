set V;                     /*The nodes of graphic;*/
/*set E, within V cross V;*/
/*check{(i,j) in E}: i != j;*/
set I{n in V};             /*The set of nodes connected to Node n by incoming fibers to n;*/
set O{n in V};             /*The set of nodes connected to Node n by outgoing fibers to n;*/
set subCa;		   /*The set of sub-carrier {0,1,...,kong-1}*/
set subCa1;                /*The set of sub-carrier {0,1,...,kong-2}*/
set S;
set D;
set NotD;
set NotS;
set subCar{w in subCa};
set subCaAll;

param kong, integer, >=0;  /*The number of sub-carriers on a fiber.*/
param B, integer, >=0;  /*The number of sub-carriers on a fiber.*/
param T{s in S,d in D}, integer, >=0;     /*The traffic demand from node n to node m;*/
param E{i in V,j in V}, integer, >=0;     


var VS{i in V,o in V,s in S,d in D,w in subCa}, binary;
var ms,integer; /*The maximum index of the sub-carriers allocated among all the fibers in the network.*/

minimize subCarriers: ms;

s.t. con1{i in V,o in V,s in S,d in D,w in subCa}: ms>=w*VS[i,o,s,d,w];
s.t. con2{s in S,d in D}: sum{w in subCa,i in I[d]} VS[i,d,s,d,w]=T[s,d];
s.t. con3{s in S,d in D}: sum{w in subCa,o in O[s]} VS[s,o,s,d,w]=T[s,d];
/*s.t. con4{i in V,o in V}: sum{w in subCa,s in S} VS[i,o,s,s,w]=0; EQ.5 makes sure that no traffic is added and dropped at the same node.*/
s.t. con5{i in V,o in V,w in subCa}: sum{s in S,d in D} VS[i,o,s,d,w]<=1; /*Sub-carrier Capacity Constraint.*/
s.t. con6{s in S,d in D,o in V}: sum{w in subCa,i in I[o]} VS[i,o,s,d,w] - sum{w in subCa,i in I[o]} VS[i,d,s,d,w] = sum{w in subCa,p in O[o]} VS[o,p,s,d,w] - sum{w in subCa,p in O[o]} VS[s,p,s,d,w] ; /*Spectrum Continuity Constraint.*/
s.t. con7{i in V,o in V,s in S,d in D,w in subCa1}: sum{w1 in subCar[w]} VS[i,o,s,d,w1]<=(VS[i,o,s,d,w]-VS[i,o,s,d,w+1]-1)*(-B);
s.t. con8{i in V,o in V,s in S,d in D,w in subCa}: sum{w1 in subCaAll} VS[i,o,s,d,w1]>=(VS[i,o,s,d,w]-1)*B + T[s,d];
s.t. con9{i in V,o in V,s in S,d in D,w in subCa}:  VS[i,o,s,d,w]+E[i,o]<=1;

data;

set V:=0 1 2 3 4 5;                     /*The nodes of graphic;*/

/*set I{n in V};             The set of nodes connected to Node n by incoming fibers to n;*/
set I[0]:=1 2 4;
set I[1]:=0 2;
set I[2]:=0 1 3;
set I[3]:=2 4 5;
set I[4]:=0 3 5;
set I[5]:=3 4;

/*set O{n in V};             The set of nodes connected to Node n by outgoing fibers to n;*/
set O[0]:=1 2 4;
set O[1]:=0 2;
set O[2]:=0 1 3;
set O[3]:=2 4 5;
set O[4]:=0 3 5;
set O[5]:=3 4;

/*set subCa;		   The set of sub-carrier {0,1,...,kong-1}*/
set subCa:=0 1 2 3 4 5; 
set subCa1:=0 1 2 3 4; 

set S := 1;
set D := 5;
set NotD :=0 1 2 3 4;
set NotS :=0 2 3 4 5;

/*set subCar[w in subCa];*/
set subCar[0]:=2 3 4 5;
set subCar[1]:=3 4 5;
set subCar[2]:=4 5;
set subCar[3]:=5;
set subCar[4]:=;
set subCar[5]:=;

set subCaAll :=0 1 2 3 4 5;

/*param kong, integer, >=0;  The number of sub-carriers on a fiber.*/
param kong:=6;
param B:=3;

/*param T{s in S,d in D}, integer, >=0;     The traffic demand from node n to node m;*/
param T[1,5]:=3;

param E:=
0 0 1
0 1 0
0 2 0
0 3 1
0 4 0
0 5 1
1 0 0
1 1 1
1 2 0
1 3 1
1 4 1
1 5 1
2 0 0
2 1 0
2 2 1
2 3 0
2 4 1
2 5 1
3 0 1
3 1 1
3 2 0
3 3 1
3 4 0
3 5 0
4 0 0
4 1 1
4 2 1
4 3 0
4 4 1
4 5 0
5 0 1
5 1 1
5 2 1
5 3 0
5 4 0
5 5 1
;



end;