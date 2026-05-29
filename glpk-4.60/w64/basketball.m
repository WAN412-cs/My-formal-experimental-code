var x;
var y;
var z;
/*minimize p:x+y+z;*/
maximize p:x+y+z;
s.t.a: x+2*y<=400;
s.t.b: 4*y+3*z=200;
s.t.c: x >= 0;
s.t.d: y >= 0;
s.t.e: z >= 10;
end;