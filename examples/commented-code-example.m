(* Example file demonstrating commented-out code detection *)

(* This is a good comment - explains what the code does *)
CalculateArea[radius_] := Pi * radius^2;

(* VIOLATION: This is commented-out code that should be deleted *)
(* OldCalculateArea[r_] := 3.14 * r * r; *)

(* Another good comment explaining the algorithm *)
(* This function uses the Newton-Raphson method to find roots *)
FindRoot[f_, x0_, iterations_] := Module[{x = x0, i},
  For[i = 1, i <= iterations, i++,
    x = x - f[x] / f'[x]
  ];
  x
];

(* VIOLATION: Multiple lines of commented code *)
(*
ProcessData[data_] := Module[{result},
  result = Map[Normalize, data];
  result = Select[result, Positive];
  result
];
*)

(* This comment describes what we should do in the future - this is fine *)
(* TODO: Add error handling for negative inputs *)

(* VIOLATION: Commented function call *)
(* result = Solve[x^2 + 3x - 4 == 0, x]; *)

(* Good comment with example usage - not code to be executed *)
(* Example: CalculateArea[5] returns approximately 78.54 *)

(* VIOLATION: Assignment that's commented out *)
(* maxIterations := 1000; *)

(* Note that this implementation is optimized for performance *)
OptimizedSum[list_] := Total[list];

(* VIOLATION: Block of code with semicolons *)
(*
x = 10;
y = 20;
z = x + y;
*)

(* Good comment explaining a parameter *)
(* The tolerance parameter controls convergence accuracy *)
Converge[f_, tolerance_: 0.001] := NestWhile[f, 1.0, Abs[#1 - #2] > tolerance &, 2];

(* VIOLATION: Module definition that's been disabled *)
(*
DebugPrint[msg_] := Module[{},
  Print["DEBUG: ", msg];
];
*)

(* This is a simple explanation - perfectly fine *)
FinalResult = CalculateArea[10];
