(* Example file demonstrating all Mathematica code quality rules *)

(* ====================
   RULE 1: Commented-Out Code (VIOLATION)
   ==================== *)

(* This is a good comment *)
ValidFunction[x_] := x^2 + 2*x + 1;

(* VIOLATION: Commented-out code *)
(* OldValidFunction[x_] := x^2 + x; *)


(* ====================
   RULE 2: TODO/FIXME Comments (VIOLATION - INFO level)
   ==================== *)

(* TODO: Optimize this algorithm for large datasets *)
SlowFunction[data_] := Map[ExpensiveOperation, data];

(* FIXME: This function fails for negative inputs *)
BrokenFunction[x_] := Sqrt[x];


(* ====================
   RULE 3: Magic Numbers (VIOLATION)
   ==================== *)

(* VIOLATION: Magic number 3.14159 *)
CircleArea[r_] := 3.14159 * r^2;

(* VIOLATION: Magic number 42 *)
DefaultThreshold[_] := 42;

(* COMPLIANT: Using named constants *)
pi = 3.14159265359;
CircleAreaGood[r_] := pi * r^2;

(* COMPLIANT: Common numbers 0, 1, 2 are acceptable *)
InitializeArray[n_] := Table[0, {i, 1, n}];


(* ====================
   RULE 4: Empty Blocks (VIOLATION)
   ==================== *)

(* VIOLATION: Empty Module *)
EmptyModule[x_] := Module[{y}, ];

(* VIOLATION: Empty Block *)
EmptyBlockFunc[x_] := Block[{z}, ];

(* COMPLIANT: Properly implemented *)
GoodModule[x_] := Module[{y},
  y = x * 2;
  y + 1
];


(* ====================
   RULE 5: Function Length (VIOLATION if > 150 lines)
   ==================== *)

(* This function is intentionally long to trigger the rule *)
VeryLongFunction[data_] := Module[{
    result, temp1, temp2, temp3, temp4, temp5,
    intermediate1, intermediate2, intermediate3
  },

  (* Step 1: Initialize *)
  result = {};
  temp1 = First[data];
  temp2 = Last[data];
  temp3 = Mean[data];
  temp4 = Median[data];
  temp5 = StandardDeviation[data];

  (* Step 2: Process *)
  intermediate1 = Map[# * 2 &, data];
  intermediate2 = Map[# + temp3 &, intermediate1];
  intermediate3 = Select[intermediate2, # > temp4 &];

  (* Step 3: More processing *)
  temp1 = Total[intermediate3];
  temp2 = Length[intermediate3];
  temp3 = temp1 / temp2;

  (* Step 4: Compute something *)
  result = Table[temp3 * i, {i, 1, 100}];

  (* Step 5: More computation *)
  result = Map[Sin, result];
  result = Map[Cos, result];
  result = Map[Tan, result];

  (* Adding many more lines to exceed 150 line threshold... *)
  (* Line 50 *)
  temp1 = result[[1]];
  temp2 = result[[2]];
  temp3 = result[[3]];
  temp4 = result[[4]];
  temp5 = result[[5]];

  (* Line 60 *)
  intermediate1 = temp1 + temp2;
  intermediate2 = temp3 + temp4;
  intermediate3 = temp5 + intermediate1;

  (* Line 70 *)
  result = Join[result, {intermediate1, intermediate2, intermediate3}];

  (* Line 80 *)
  result = Sort[result];
  result = Reverse[result];
  result = Take[result, 50];

  (* Line 90 *)
  temp1 = First[result];
  temp2 = Last[result];

  (* Line 100 *)
  result = Map[# / temp1 &, result];

  (* Line 110 *)
  result = Select[result, # > 0.5 &];

  (* Line 120 *)
  result = Map[Round[#, 0.01] &, result];

  (* Line 130 *)
  result = DeleteDuplicates[result];

  (* Line 140 *)
  result = Prepend[result, temp3];

  (* Line 150 *)
  result = Append[result, temp4];

  (* Line 160 *)
  result = Insert[result, temp5, 10];

  (* Return *)
  result
];


(* ====================
   RULE 6: File Length (VIOLATION if > 1000 lines)
   ==================== *)

(* This file is short, so no violation for file length *)
(* To trigger this rule, the file would need to exceed 1000 lines *)


(* ====================
   COMPLIANT EXAMPLES - No violations
   ==================== *)

(* Good function with clear purpose and reasonable length *)
CalculateDistance[p1_, p2_] := Module[{dx, dy},
  dx = p1[[1]] - p2[[1]];
  dy = p1[[2]] - p2[[2]];
  Sqrt[dx^2 + dy^2]
];

(* Using constants instead of magic numbers *)
gravitationalConstant = 6.67430 * 10^-11;
CalculateGravitationalForce[m1_, m2_, r_] :=
  gravitationalConstant * m1 * m2 / r^2;

(* Proper documentation comment *)
(* This function implements the quicksort algorithm *)
QuickSort[list_] := Module[{pivot, lesser, greater},
  If[Length[list] <= 1,
    list,
    pivot = First[list];
    lesser = Select[Rest[list], # <= pivot &];
    greater = Select[Rest[list], # > pivot &];
    Join[QuickSort[lesser], {pivot}, QuickSort[greater]]
  ]
];
