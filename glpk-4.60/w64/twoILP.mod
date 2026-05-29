var x1,integer;
var x2, integer;
minimize shPath: x1+x2;
s.t. c1:3*x1+4*x2>=25;
s.t. c2:5*x1+6*x2>=35;
s.t. c3:x1>=0;
s.t. c4:x2>=1;
s.t. c5:4*x1+x2>=30;
end;