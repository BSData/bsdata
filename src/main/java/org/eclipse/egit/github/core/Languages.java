/******************************************************************************
 *  Copyright (c) 2011 GitHub Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *    Kevin Sawicki (GitHub Inc.) - initial API and implementation
 *    Michael Keppler - update languages 2020
 *****************************************************************************/
package org.eclipse.egit.github.core;

import java.util.Arrays;

/**
 * Programming languages available in github search at
 * https://github.com/search/advanced.
 */
public final class Languages {

	private Languages() {
		// utility class
	}

	private static final String[] languages = new String[] {
			"ActionScript", //$NON-NLS-1$
			"C", //$NON-NLS-1$
			"C#", //$NON-NLS-1$
			"C++", //$NON-NLS-1$
			"Clojure", //$NON-NLS-1$
			"CoffeeScript", //$NON-NLS-1$
			"CSS", //$NON-NLS-1$
			"Go", //$NON-NLS-1$
			"Haskell", //$NON-NLS-1$
			"HTML", //$NON-NLS-1$
			"Java", //$NON-NLS-1$
			"JavaScript", //$NON-NLS-1$
			"Lua", //$NON-NLS-1$
			"MATLAB", //$NON-NLS-1$
			"Objective-C", //$NON-NLS-1$
			"Perl", //$NON-NLS-1$
			"PHP", //$NON-NLS-1$
			"Python", //$NON-NLS-1$
			"R", //$NON-NLS-1$
			"Ruby", //$NON-NLS-1$
			"Scala", //$NON-NLS-1$
			"Shell", //$NON-NLS-1$
			"Swift", //$NON-NLS-1$
			"TeX", //$NON-NLS-1$
			"Vim script", //$NON-NLS-1$
			"1C Enterprise", //$NON-NLS-1$
			"4D", //$NON-NLS-1$
			"ABAP", //$NON-NLS-1$
			"ABNF", //$NON-NLS-1$
			"Ada", //$NON-NLS-1$
			"Adobe Font Metrics", //$NON-NLS-1$
			"Agda", //$NON-NLS-1$
			"AGS Script", //$NON-NLS-1$
			"Alloy", //$NON-NLS-1$
			"Alpine Abuild", //$NON-NLS-1$
			"Altium Designer", //$NON-NLS-1$
			"AMPL", //$NON-NLS-1$
			"AngelScript", //$NON-NLS-1$
			"Ant Build System", //$NON-NLS-1$
			"ANTLR", //$NON-NLS-1$
			"ApacheConf", //$NON-NLS-1$
			"Apex", //$NON-NLS-1$
			"API Blueprint", //$NON-NLS-1$
			"APL", //$NON-NLS-1$
			"Apollo Guidance Computer", //$NON-NLS-1$
			"AppleScript", //$NON-NLS-1$
			"Arc", //$NON-NLS-1$
			"AsciiDoc", //$NON-NLS-1$
			"ASN.1", //$NON-NLS-1$
			"ASP", //$NON-NLS-1$
			"AspectJ", //$NON-NLS-1$
			"Assembly", //$NON-NLS-1$
			"Asymptote", //$NON-NLS-1$
			"ATS", //$NON-NLS-1$
			"Augeas", //$NON-NLS-1$
			"AutoHotkey", //$NON-NLS-1$
			"AutoIt", //$NON-NLS-1$
			"Awk", //$NON-NLS-1$
			"Ballerina", //$NON-NLS-1$
			"Batchfile", //$NON-NLS-1$
			"Befunge", //$NON-NLS-1$
			"BibTeX", //$NON-NLS-1$
			"Bison", //$NON-NLS-1$
			"BitBake", //$NON-NLS-1$
			"Blade", //$NON-NLS-1$
			"BlitzBasic", //$NON-NLS-1$
			"BlitzMax", //$NON-NLS-1$
			"Bluespec", //$NON-NLS-1$
			"Boo", //$NON-NLS-1$
			"Brainfuck", //$NON-NLS-1$
			"Brightscript", //$NON-NLS-1$
			"C-ObjDump", //$NON-NLS-1$
			"C2hs Haskell", //$NON-NLS-1$
			"Cabal Config", //$NON-NLS-1$
			"Cap'n Proto", //$NON-NLS-1$
			"CartoCSS", //$NON-NLS-1$
			"Ceylon", //$NON-NLS-1$
			"Chapel", //$NON-NLS-1$
			"Charity", //$NON-NLS-1$
			"ChucK", //$NON-NLS-1$
			"Cirru", //$NON-NLS-1$
			"Clarion", //$NON-NLS-1$
			"Clean", //$NON-NLS-1$
			"Click", //$NON-NLS-1$
			"CLIPS", //$NON-NLS-1$
			"Closure Templates", //$NON-NLS-1$
			"Cloud Firestore Security Rules", //$NON-NLS-1$
			"CMake", //$NON-NLS-1$
			"COBOL", //$NON-NLS-1$
			"CodeQL", //$NON-NLS-1$
			"ColdFusion", //$NON-NLS-1$
			"ColdFusion CFC", //$NON-NLS-1$
			"COLLADA", //$NON-NLS-1$
			"Common Lisp", //$NON-NLS-1$
			"Common Workflow Language", //$NON-NLS-1$
			"Component Pascal", //$NON-NLS-1$
			"CoNLL-U", //$NON-NLS-1$
			"Cool", //$NON-NLS-1$
			"Coq", //$NON-NLS-1$
			"Cpp-ObjDump", //$NON-NLS-1$
			"Creole", //$NON-NLS-1$
			"Crystal", //$NON-NLS-1$
			"CSON", //$NON-NLS-1$
			"Csound", //$NON-NLS-1$
			"Csound Document", //$NON-NLS-1$
			"Csound Score", //$NON-NLS-1$
			"CSV", //$NON-NLS-1$
			"Cuda", //$NON-NLS-1$
			"cURL Config", //$NON-NLS-1$
			"CWeb", //$NON-NLS-1$
			"Cycript", //$NON-NLS-1$
			"Cython", //$NON-NLS-1$
			"D", //$NON-NLS-1$
			"D-ObjDump", //$NON-NLS-1$
			"Darcs Patch", //$NON-NLS-1$
			"Dart", //$NON-NLS-1$
			"DataWeave", //$NON-NLS-1$
			"desktop", //$NON-NLS-1$
			"Dhall", //$NON-NLS-1$
			"Diff", //$NON-NLS-1$
			"DIGITAL Command Language", //$NON-NLS-1$
			"dircolors", //$NON-NLS-1$
			"DirectX 3D File", //$NON-NLS-1$
			"DM", //$NON-NLS-1$
			"DNS Zone", //$NON-NLS-1$
			"Dockerfile", //$NON-NLS-1$
			"Dogescript", //$NON-NLS-1$
			"DTrace", //$NON-NLS-1$
			"Dylan", //$NON-NLS-1$
			"E", //$NON-NLS-1$
			"Eagle", //$NON-NLS-1$
			"Easybuild", //$NON-NLS-1$
			"EBNF", //$NON-NLS-1$
			"eC", //$NON-NLS-1$
			"Ecere Projects", //$NON-NLS-1$
			"ECL", //$NON-NLS-1$
			"ECLiPSe", //$NON-NLS-1$
			"EditorConfig", //$NON-NLS-1$
			"Edje Data Collection", //$NON-NLS-1$
			"edn", //$NON-NLS-1$
			"Eiffel", //$NON-NLS-1$
			"EJS", //$NON-NLS-1$
			"Elixir", //$NON-NLS-1$
			"Elm", //$NON-NLS-1$
			"Emacs Lisp", //$NON-NLS-1$
			"EmberScript", //$NON-NLS-1$
			"EML", //$NON-NLS-1$
			"EQ", //$NON-NLS-1$
			"Erlang", //$NON-NLS-1$
			"F#", //$NON-NLS-1$
			"F*", //$NON-NLS-1$
			"Factor", //$NON-NLS-1$
			"Fancy", //$NON-NLS-1$
			"Fantom", //$NON-NLS-1$
			"Faust", //$NON-NLS-1$
			"FIGlet Font", //$NON-NLS-1$
			"Filebench WML", //$NON-NLS-1$
			"Filterscript", //$NON-NLS-1$
			"fish", //$NON-NLS-1$
			"FLUX", //$NON-NLS-1$
			"Formatted", //$NON-NLS-1$
			"Forth", //$NON-NLS-1$
			"Fortran", //$NON-NLS-1$
			"FreeMarker", //$NON-NLS-1$
			"Frege", //$NON-NLS-1$
			"G-code", //$NON-NLS-1$
			"Game Maker Language", //$NON-NLS-1$
			"GAML", //$NON-NLS-1$
			"GAMS", //$NON-NLS-1$
			"GAP", //$NON-NLS-1$
			"GCC Machine Description", //$NON-NLS-1$
			"GDB", //$NON-NLS-1$
			"GDScript", //$NON-NLS-1$
			"Genie", //$NON-NLS-1$
			"Genshi", //$NON-NLS-1$
			"Gentoo Ebuild", //$NON-NLS-1$
			"Gentoo Eclass", //$NON-NLS-1$
			"Gerber Image", //$NON-NLS-1$
			"Gettext Catalog", //$NON-NLS-1$
			"Gherkin", //$NON-NLS-1$
			"Git Attributes", //$NON-NLS-1$
			"Git Config", //$NON-NLS-1$
			"GLSL", //$NON-NLS-1$
			"Glyph", //$NON-NLS-1$
			"Glyph Bitmap Distribution Format", //$NON-NLS-1$
			"GN", //$NON-NLS-1$
			"Gnuplot", //$NON-NLS-1$
			"Golo", //$NON-NLS-1$
			"Gosu", //$NON-NLS-1$
			"Grace", //$NON-NLS-1$
			"Gradle", //$NON-NLS-1$
			"Grammatical Framework", //$NON-NLS-1$
			"Graph Modeling Language", //$NON-NLS-1$
			"GraphQL", //$NON-NLS-1$
			"Graphviz (DOT)", //$NON-NLS-1$
			"Groovy", //$NON-NLS-1$
			"Groovy Server Pages", //$NON-NLS-1$
			"Hack", //$NON-NLS-1$
			"Haml", //$NON-NLS-1$
			"Handlebars", //$NON-NLS-1$
			"HAProxy", //$NON-NLS-1$
			"Harbour", //$NON-NLS-1$
			"Haxe", //$NON-NLS-1$
			"HCL", //$NON-NLS-1$
			"HiveQL", //$NON-NLS-1$
			"HLSL", //$NON-NLS-1$
			"HolyC", //$NON-NLS-1$
			"HTML+Django", //$NON-NLS-1$
			"HTML+ECR", //$NON-NLS-1$
			"HTML+EEX", //$NON-NLS-1$
			"HTML+ERB", //$NON-NLS-1$
			"HTML+PHP", //$NON-NLS-1$
			"HTML+Razor", //$NON-NLS-1$
			"HTTP", //$NON-NLS-1$
			"HXML", //$NON-NLS-1$
			"Hy", //$NON-NLS-1$
			"HyPhy", //$NON-NLS-1$
			"IDL", //$NON-NLS-1$
			"Idris", //$NON-NLS-1$
			"Ignore List", //$NON-NLS-1$
			"IGOR Pro", //$NON-NLS-1$
			"Inform 7", //$NON-NLS-1$
			"INI", //$NON-NLS-1$
			"Inno Setup", //$NON-NLS-1$
			"Io", //$NON-NLS-1$
			"Ioke", //$NON-NLS-1$
			"IRC log", //$NON-NLS-1$
			"Isabelle", //$NON-NLS-1$
			"Isabelle ROOT", //$NON-NLS-1$
			"J", //$NON-NLS-1$
			"Jasmin", //$NON-NLS-1$
			"Java Properties", //$NON-NLS-1$
			"Java Server Pages", //$NON-NLS-1$
			"JavaScript+ERB", //$NON-NLS-1$
			"JFlex", //$NON-NLS-1$
			"Jison", //$NON-NLS-1$
			"Jison Lex", //$NON-NLS-1$
			"Jolie", //$NON-NLS-1$
			"JSON", //$NON-NLS-1$
			"JSON with Comments", //$NON-NLS-1$
			"JSON5", //$NON-NLS-1$
			"JSONiq", //$NON-NLS-1$
			"JSONLD", //$NON-NLS-1$
			"Jsonnet", //$NON-NLS-1$
			"JSX", //$NON-NLS-1$
			"Julia", //$NON-NLS-1$
			"Jupyter Notebook", //$NON-NLS-1$
			"KiCad Layout", //$NON-NLS-1$
			"KiCad Legacy Layout", //$NON-NLS-1$
			"KiCad Schematic", //$NON-NLS-1$
			"Kit", //$NON-NLS-1$
			"Kotlin", //$NON-NLS-1$
			"KRL", //$NON-NLS-1$
			"LabVIEW", //$NON-NLS-1$
			"Lasso", //$NON-NLS-1$
			"Latte", //$NON-NLS-1$
			"Lean", //$NON-NLS-1$
			"Less", //$NON-NLS-1$
			"Lex", //$NON-NLS-1$
			"LFE", //$NON-NLS-1$
			"LilyPond", //$NON-NLS-1$
			"Limbo", //$NON-NLS-1$
			"Linker Script", //$NON-NLS-1$
			"Linux Kernel Module", //$NON-NLS-1$
			"Liquid", //$NON-NLS-1$
			"Literate Agda", //$NON-NLS-1$
			"Literate CoffeeScript", //$NON-NLS-1$
			"Literate Haskell", //$NON-NLS-1$
			"LiveScript", //$NON-NLS-1$
			"LLVM", //$NON-NLS-1$
			"Logos", //$NON-NLS-1$
			"Logtalk", //$NON-NLS-1$
			"LOLCODE", //$NON-NLS-1$
			"LookML", //$NON-NLS-1$
			"LoomScript", //$NON-NLS-1$
			"LSL", //$NON-NLS-1$
			"LTspice Symbol", //$NON-NLS-1$
			"M", //$NON-NLS-1$
			"M4", //$NON-NLS-1$
			"M4Sugar", //$NON-NLS-1$
			"Makefile", //$NON-NLS-1$
			"Mako", //$NON-NLS-1$
			"Markdown", //$NON-NLS-1$
			"Marko", //$NON-NLS-1$
			"Mask", //$NON-NLS-1$
			"Mathematica", //$NON-NLS-1$
			"Maven POM", //$NON-NLS-1$
			"Max", //$NON-NLS-1$
			"MAXScript", //$NON-NLS-1$
			"mcfunction", //$NON-NLS-1$
			"MediaWiki", //$NON-NLS-1$
			"Mercury", //$NON-NLS-1$
			"Meson", //$NON-NLS-1$
			"Metal", //$NON-NLS-1$
			"Microsoft Developer Studio Project", //$NON-NLS-1$
			"MiniD", //$NON-NLS-1$
			"Mirah", //$NON-NLS-1$
			"mIRC Script", //$NON-NLS-1$
			"MLIR", //$NON-NLS-1$
			"Modelica", //$NON-NLS-1$
			"Modula-2", //$NON-NLS-1$
			"Modula-3", //$NON-NLS-1$
			"Module Management System", //$NON-NLS-1$
			"Monkey", //$NON-NLS-1$
			"Moocode", //$NON-NLS-1$
			"MoonScript", //$NON-NLS-1$
			"Motorola 68K Assembly", //$NON-NLS-1$
			"MQL4", //$NON-NLS-1$
			"MQL5", //$NON-NLS-1$
			"MTML", //$NON-NLS-1$
			"MUF", //$NON-NLS-1$
			"mupad", //$NON-NLS-1$
			"Muse", //$NON-NLS-1$
			"Myghty", //$NON-NLS-1$
			"nanorc", //$NON-NLS-1$
			"NASL", //$NON-NLS-1$
			"NCL", //$NON-NLS-1$
			"Nearley", //$NON-NLS-1$
			"Nemerle", //$NON-NLS-1$
			"nesC", //$NON-NLS-1$
			"NetLinx", //$NON-NLS-1$
			"NetLinx+ERB", //$NON-NLS-1$
			"NetLogo", //$NON-NLS-1$
			"NewLisp", //$NON-NLS-1$
			"Nextflow", //$NON-NLS-1$
			"Nginx", //$NON-NLS-1$
			"Nim", //$NON-NLS-1$
			"Ninja", //$NON-NLS-1$
			"Nit", //$NON-NLS-1$
			"Nix", //$NON-NLS-1$
			"NL", //$NON-NLS-1$
			"NPM Config", //$NON-NLS-1$
			"NSIS", //$NON-NLS-1$
			"Nu", //$NON-NLS-1$
			"NumPy", //$NON-NLS-1$
			"ObjDump", //$NON-NLS-1$
			"Object Data Instance Notation", //$NON-NLS-1$
			"Objective-C++", //$NON-NLS-1$
			"Objective-J", //$NON-NLS-1$
			"ObjectScript", //$NON-NLS-1$
			"OCaml", //$NON-NLS-1$
			"Odin", //$NON-NLS-1$
			"Omgrofl", //$NON-NLS-1$
			"ooc", //$NON-NLS-1$
			"Opa", //$NON-NLS-1$
			"Opal", //$NON-NLS-1$
			"Open Policy Agent", //$NON-NLS-1$
			"OpenCL", //$NON-NLS-1$
			"OpenEdge ABL", //$NON-NLS-1$
			"OpenQASM", //$NON-NLS-1$
			"OpenRC runscript", //$NON-NLS-1$
			"OpenSCAD", //$NON-NLS-1$
			"OpenStep Property List", //$NON-NLS-1$
			"OpenType Feature File", //$NON-NLS-1$
			"Org", //$NON-NLS-1$
			"Ox", //$NON-NLS-1$
			"Oxygene", //$NON-NLS-1$
			"Oz", //$NON-NLS-1$
			"P4", //$NON-NLS-1$
			"Pan", //$NON-NLS-1$
			"Papyrus", //$NON-NLS-1$
			"Parrot", //$NON-NLS-1$
			"Parrot Assembly", //$NON-NLS-1$
			"Parrot Internal Representation", //$NON-NLS-1$
			"Pascal", //$NON-NLS-1$
			"Pawn", //$NON-NLS-1$
			"Pep8", //$NON-NLS-1$
			"Pic", //$NON-NLS-1$
			"Pickle", //$NON-NLS-1$
			"PicoLisp", //$NON-NLS-1$
			"PigLatin", //$NON-NLS-1$
			"Pike", //$NON-NLS-1$
			"PlantUML", //$NON-NLS-1$
			"PLpgSQL", //$NON-NLS-1$
			"PLSQL", //$NON-NLS-1$
			"Pod", //$NON-NLS-1$
			"Pod 6", //$NON-NLS-1$
			"PogoScript", //$NON-NLS-1$
			"Pony", //$NON-NLS-1$
			"PostCSS", //$NON-NLS-1$
			"PostScript", //$NON-NLS-1$
			"POV-Ray SDL", //$NON-NLS-1$
			"PowerBuilder", //$NON-NLS-1$
			"PowerShell", //$NON-NLS-1$
			"Prisma", //$NON-NLS-1$
			"Processing", //$NON-NLS-1$
			"Proguard", //$NON-NLS-1$
			"Prolog", //$NON-NLS-1$
			"Propeller Spin", //$NON-NLS-1$
			"Protocol Buffer", //$NON-NLS-1$
			"Public Key", //$NON-NLS-1$
			"Pug", //$NON-NLS-1$
			"Puppet", //$NON-NLS-1$
			"Pure Data", //$NON-NLS-1$
			"PureBasic", //$NON-NLS-1$
			"PureScript", //$NON-NLS-1$
			"Python console", //$NON-NLS-1$
			"Python traceback", //$NON-NLS-1$
			"q", //$NON-NLS-1$
			"QMake", //$NON-NLS-1$
			"QML", //$NON-NLS-1$
			"Quake", //$NON-NLS-1$
			"Racket", //$NON-NLS-1$
			"Ragel", //$NON-NLS-1$
			"Raku", //$NON-NLS-1$
			"RAML", //$NON-NLS-1$
			"Rascal", //$NON-NLS-1$
			"Raw token data", //$NON-NLS-1$
			"RDoc", //$NON-NLS-1$
			"Readline Config", //$NON-NLS-1$
			"REALbasic", //$NON-NLS-1$
			"Reason", //$NON-NLS-1$
			"Rebol", //$NON-NLS-1$
			"Red", //$NON-NLS-1$
			"Redcode", //$NON-NLS-1$
			"Regular Expression", //$NON-NLS-1$
			"Ren'Py", //$NON-NLS-1$
			"RenderScript", //$NON-NLS-1$
			"reStructuredText", //$NON-NLS-1$
			"REXX", //$NON-NLS-1$
			"RHTML", //$NON-NLS-1$
			"Rich Text Format", //$NON-NLS-1$
			"Ring", //$NON-NLS-1$
			"Riot", //$NON-NLS-1$
			"RMarkdown", //$NON-NLS-1$
			"RobotFramework", //$NON-NLS-1$
			"Roff", //$NON-NLS-1$
			"Roff Manpage", //$NON-NLS-1$
			"Rouge", //$NON-NLS-1$
			"RPC", //$NON-NLS-1$
			"RPM Spec", //$NON-NLS-1$
			"RUNOFF", //$NON-NLS-1$
			"Rust", //$NON-NLS-1$
			"Sage", //$NON-NLS-1$
			"SaltStack", //$NON-NLS-1$
			"SAS", //$NON-NLS-1$
			"Sass", //$NON-NLS-1$
			"Scaml", //$NON-NLS-1$
			"Scheme", //$NON-NLS-1$
			"Scilab", //$NON-NLS-1$
			"SCSS", //$NON-NLS-1$
			"sed", //$NON-NLS-1$
			"Self", //$NON-NLS-1$
			"ShaderLab", //$NON-NLS-1$
			"ShellSession", //$NON-NLS-1$
			"Shen", //$NON-NLS-1$
			"Slash", //$NON-NLS-1$
			"Slice", //$NON-NLS-1$
			"Slim", //$NON-NLS-1$
			"Smali", //$NON-NLS-1$
			"Smalltalk", //$NON-NLS-1$
			"Smarty", //$NON-NLS-1$
			"SmPL", //$NON-NLS-1$
			"SMT", //$NON-NLS-1$
			"Solidity", //$NON-NLS-1$
			"SourcePawn", //$NON-NLS-1$
			"SPARQL", //$NON-NLS-1$
			"Spline Font Database", //$NON-NLS-1$
			"SQF", //$NON-NLS-1$
			"SQL", //$NON-NLS-1$
			"SQLPL", //$NON-NLS-1$
			"Squirrel", //$NON-NLS-1$
			"SRecode Template", //$NON-NLS-1$
			"SSH Config", //$NON-NLS-1$
			"Stan", //$NON-NLS-1$
			"Standard ML", //$NON-NLS-1$
			"Starlark", //$NON-NLS-1$
			"Stata", //$NON-NLS-1$
			"STON", //$NON-NLS-1$
			"Stylus", //$NON-NLS-1$
			"SubRip Text", //$NON-NLS-1$
			"SugarSS", //$NON-NLS-1$
			"SuperCollider", //$NON-NLS-1$
			"Svelte", //$NON-NLS-1$
			"SVG", //$NON-NLS-1$
			"SWIG", //$NON-NLS-1$
			"SystemVerilog", //$NON-NLS-1$
			"Tcl", //$NON-NLS-1$
			"Tcsh", //$NON-NLS-1$
			"Tea", //$NON-NLS-1$
			"Terra", //$NON-NLS-1$
			"Texinfo", //$NON-NLS-1$
			"Text", //$NON-NLS-1$
			"Textile", //$NON-NLS-1$
			"Thrift", //$NON-NLS-1$
			"TI Program", //$NON-NLS-1$
			"TLA", //$NON-NLS-1$
			"TOML", //$NON-NLS-1$
			"TSQL", //$NON-NLS-1$
			"TSX", //$NON-NLS-1$
			"Turing", //$NON-NLS-1$
			"Turtle", //$NON-NLS-1$
			"Twig", //$NON-NLS-1$
			"TXL", //$NON-NLS-1$
			"Type Language", //$NON-NLS-1$
			"TypeScript", //$NON-NLS-1$
			"Unified Parallel C", //$NON-NLS-1$
			"Unity3D Asset", //$NON-NLS-1$
			"Unix Assembly", //$NON-NLS-1$
			"Uno", //$NON-NLS-1$
			"UnrealScript", //$NON-NLS-1$
			"UrWeb", //$NON-NLS-1$
			"V", //$NON-NLS-1$
			"Vala", //$NON-NLS-1$
			"VBA", //$NON-NLS-1$
			"VBScript", //$NON-NLS-1$
			"VCL", //$NON-NLS-1$
			"Verilog", //$NON-NLS-1$
			"VHDL", //$NON-NLS-1$
			"Vim Snippet", //$NON-NLS-1$
			"Visual Basic .NET", //$NON-NLS-1$
			"Volt", //$NON-NLS-1$
			"Vue", //$NON-NLS-1$
			"Wavefront Material", //$NON-NLS-1$
			"Wavefront Object", //$NON-NLS-1$
			"wdl", //$NON-NLS-1$
			"Web Ontology Language", //$NON-NLS-1$
			"WebAssembly", //$NON-NLS-1$
			"WebIDL", //$NON-NLS-1$
			"WebVTT", //$NON-NLS-1$
			"Wget Config", //$NON-NLS-1$
			"Windows Registry Entries", //$NON-NLS-1$
			"wisp", //$NON-NLS-1$
			"Wollok", //$NON-NLS-1$
			"World of Warcraft Addon Data", //$NON-NLS-1$
			"X BitMap", //$NON-NLS-1$
			"X Font Directory Index", //$NON-NLS-1$
			"X PixMap", //$NON-NLS-1$
			"X10", //$NON-NLS-1$
			"xBase", //$NON-NLS-1$
			"XC", //$NON-NLS-1$
			"XCompose", //$NON-NLS-1$
			"XML", //$NON-NLS-1$
			"XML Property List", //$NON-NLS-1$
			"Xojo", //$NON-NLS-1$
			"XPages", //$NON-NLS-1$
			"XProc", //$NON-NLS-1$
			"XQuery", //$NON-NLS-1$
			"XS", //$NON-NLS-1$
			"XSLT", //$NON-NLS-1$
			"Xtend", //$NON-NLS-1$
			"Yacc", //$NON-NLS-1$
			"YAML", //$NON-NLS-1$
			"YANG", //$NON-NLS-1$
			"YARA", //$NON-NLS-1$
			"YASnippet", //$NON-NLS-1$
			"ZAP", //$NON-NLS-1$
			"Zeek", //$NON-NLS-1$
			"ZenScript", //$NON-NLS-1$
			"Zephir", //$NON-NLS-1$
			"Zig", //$NON-NLS-1$
			"ZIL", //$NON-NLS-1$
			"Zimpl", //$NON-NLS-1$
	};

	/**
	 * Get sorted languages
	 *
	 * @return sorted languages
	 */
	public static String[] getLanguages() {
		return Arrays.stream(languages).sorted(String.CASE_INSENSITIVE_ORDER)
				.toArray(String[]::new);
	}
}
