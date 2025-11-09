#!/usr/bin/env wolframscript
(* ::Package:: *)

(*
 * CoverageUtils.wl - Utility package for SonarQube coverage integration
 *
 * This package provides helper functions to export Mathematica test coverage
 * data in formats compatible with SonarQube code quality analysis.
 *
 * Author: Bryan C. Everly
 * Version: 1.0.0
 * License: GPLv3
 *
 * Usage:
 *   Get["CoverageUtils.wl"]
 *   ExportCoverageJSON[coverageData, "coverage/coverage.json"]
 *)

BeginPackage["CoverageUtils`"]

ExportCoverageJSON::usage =
  "ExportCoverageJSON[data, file] exports coverage data to JSON format.\n\
   \n\
   Parameters:\n\
     data - Association of coverage data with file paths as keys\n\
     file - Output file path for JSON export\n\
   \n\
   Each file's coverage data should have the structure:\n\
     <|\"FileName\" -> name,\n\
       \"FullPath\" -> path,\n\
       \"TotalLines\" -> n,\n\
       \"CodeLines\" -> n,\n\
       \"CoveredLines\" -> n,\n\
       \"Coverage\" -> 0.0 to 1.0,\n\
       \"LineCoverage\" -> <|lineNum -> <|\"Line\"->n, \"IsCode\"->bool, \"Hits\"->n|>|>\n\
     |>\n\
   \n\
   Returns:\n\
     The file path if successful, $Failed otherwise.\n\
   \n\
   Example:\n\
     coverageData = <|\n\
       \"/path/to/Source.wl\" -> <|\n\
         \"FileName\" -> \"Source\",\n\
         \"Coverage\" -> 0.75,\n\
         ...\n\
       |>\n\
     |>;\n\
     ExportCoverageJSON[coverageData, \"coverage.json\"]";

ValidateCoverageData::usage =
  "ValidateCoverageData[data] validates coverage data structure.\n\
   \n\
   Returns True if data is valid, False otherwise with messages.";

Begin["`Private`"]

(*
 * ValidateCoverageData - Validates the structure of coverage data
 *
 * Checks that the coverage data has all required fields and correct types.
 *)
ValidateCoverageData[data_Association] := Module[{valid = True, requiredKeys},
  requiredKeys = {"FileName", "FullPath", "TotalLines", "CodeLines",
                  "CoveredLines", "Coverage", "LineCoverage"};

  (* Check each file's data *)
  Do[
    Module[{fileData = data[key], missing},
      (* Check for required keys *)
      missing = Complement[requiredKeys, Keys[fileData]];
      If[Length[missing] > 0,
        Message[ValidateCoverageData::missing, key, missing];
        valid = False;
      ];

      (* Validate types *)
      If[KeyExistsQ[fileData, "FileName"] && !StringQ[fileData["FileName"]],
        Message[ValidateCoverageData::type, key, "FileName", "String"];
        valid = False;
      ];

      If[KeyExistsQ[fileData, "Coverage"] && !NumericQ[fileData["Coverage"]],
        Message[ValidateCoverageData::type, key, "Coverage", "Numeric"];
        valid = False;
      ];

      If[KeyExistsQ[fileData, "LineCoverage"] && !AssociationQ[fileData["LineCoverage"]],
        Message[ValidateCoverageData::type, key, "LineCoverage", "Association"];
        valid = False;
      ];
    ],
    {key, Keys[data]}
  ];

  valid
]

ValidateCoverageData[_] := False

ValidateCoverageData::missing = "File `1` is missing required keys: `2`";
ValidateCoverageData::type = "File `1`: field `2` should be `3`";

(*
 * ExportCoverageJSON - Exports coverage data to JSON format
 *
 * This function transforms the nested Association structure from Mathematica
 * coverage analysis into a clean JSON format that can be easily parsed by
 * the SonarQube plugin.
 *
 * The JSON transformation:
 * 1. Converts file-path-keyed associations to a list
 * 2. Converts line-number-keyed associations to a list
 * 3. Removes the "Code" field from LineCoverage (would bloat the file)
 * 4. Preserves all metrics and hit counts
 *)
ExportCoverageJSON[coverageData_Association, jsonFile_String] := Module[
  {jsonData, exportResult},

  (* Validate input data *)
  If[!ValidateCoverageData[coverageData],
    Message[ExportCoverageJSON::invalid];
    Return[$Failed]
  ];

  (* Transform to JSON-friendly format *)
  jsonData = Map[
    Function[{fileData},
      <|
        "FileName" -> fileData["FileName"],
        "FullPath" -> fileData["FullPath"],
        "TotalLines" -> fileData["TotalLines"],
        "CodeLines" -> fileData["CodeLines"],
        "CoveredLines" -> fileData["CoveredLines"],
        "Coverage" -> fileData["Coverage"],
        "LineCoverage" -> Map[
          Function[{lineData},
            <|
              "Line" -> lineData["Line"],
              "IsCode" -> lineData["IsCode"],
              "Hits" -> lineData["Hits"]
            |>
          ],
          Values[fileData["LineCoverage"]]
        ]
      |>
    ],
    coverageData
  ];

  (* Export to JSON *)
  exportResult = Export[jsonFile, Values[jsonData], "JSON"];

  If[exportResult === $Failed,
    Message[ExportCoverageJSON::export, jsonFile];
    $Failed,
    exportResult
  ]
]

ExportCoverageJSON::invalid = "Coverage data validation failed. Check structure.";
ExportCoverageJSON::export = "Failed to export JSON to file: `1`";

End[]

EndPackage[]

(*
 * Package loaded successfully
 * To use: Get["CoverageUtils.wl"]
 *)
