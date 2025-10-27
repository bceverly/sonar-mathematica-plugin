(* Sample Mathematica file for testing duplication detection *)

(* Function to calculate factorial *)
factorial[n_] := Module[{result, i},
  result = 1;
  For[i = 1, i <= n, i++,
    result = result * i
  ];
  result
]

(* This is duplicated logic - should be detected by CPD *)
factorial2[n_] := Module[{result, i},
  result = 1;
  For[i = 1, i <= n, i++,
    result = result * i
  ];
  result
]

(* Function to calculate fibonacci *)
fibonacci[n_] := Module[{a, b, temp, i},
  a = 0;
  b = 1;
  For[i = 0, i < n, i++,
    temp = a + b;
    a = b;
    b = temp
  ];
  a
]

(* Another duplicated fibonacci implementation *)
fibonacci2[n_] := Module[{a, b, temp, i},
  a = 0;
  b = 1;
  For[i = 0, i < n, i++,
    temp = a + b;
    a = b;
    b = temp
  ];
  a
]

(* String operations *)
processString[str_String] := Module[{upper, lower},
  upper = ToUpperCase[str];
  lower = ToLowerCase[str];
  StringJoin[upper, " and ", lower]
]

(* List operations *)
processList[list_List] := Module[{sum, mean, max, min},
  sum = Total[list];
  mean = Mean[list];
  max = Max[list];
  min = Min[list];
  <|"sum" -> sum, "mean" -> mean, "max" -> max, "min" -> min|>
]
