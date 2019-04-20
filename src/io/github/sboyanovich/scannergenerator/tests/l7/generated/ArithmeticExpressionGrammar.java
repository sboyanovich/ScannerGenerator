package io.github.sboyanovich.scannergenerator.tests.l7.generated;

import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.DomainEOP;
import io.github.sboyanovich.scannergenerator.tests.l7.aux.UnifiedAlphabetSymbol;
import java.util.*;
public class ArithmeticExpressionGrammar {
	private static List<String> terminalNames;
	private static List<String> nonTerminalNames;
	static {
		terminalNames = new ArrayList<>();
		nonTerminalNames = new ArrayList<>();
		terminalNames.add("n");
		terminalNames.add("\\(");
		terminalNames.add("\\)");
		terminalNames.add("*");
		terminalNames.add("+");
		nonTerminalNames.add("F");
		nonTerminalNames.add("E");
		nonTerminalNames.add("T");
		nonTerminalNames.add("T1");
		nonTerminalNames.add("E1");
	}
	public static Map<Integer, Domain> getTermInterpretation(Map<String, Domain> domainNames) {
		Map<Integer, Domain> result = new HashMap<>();
		for(int i = 0; i < terminalNames.size(); i++) {
			result.put(i, domainNames.get(terminalNames.get(i)));
		}
		result.put(terminalNames.size(), DomainEOP.END_OF_PROGRAM);
		return result;
	}
	public static List<String> getNonTerminalNames() {
		return Collections.unmodifiableList(nonTerminalNames);
	}
	public static int getAxiom() {
		return 1;
	}
	public static int[][] getPredictionTable() {
		int[][] result = new int[5][6];
		result[0][0] = 0;
		result[0][1] = 1;
		result[0][2] = -1;
		result[0][3] = -1;
		result[0][4] = -1;
		result[0][5] = -1;
		result[1][0] = 0;
		result[1][1] = 0;
		result[1][2] = -1;
		result[1][3] = -1;
		result[1][4] = -1;
		result[1][5] = -1;
		result[2][0] = 0;
		result[2][1] = 0;
		result[2][2] = -1;
		result[2][3] = -1;
		result[2][4] = -1;
		result[2][5] = -1;
		result[3][0] = -1;
		result[3][1] = -1;
		result[3][2] = 1;
		result[3][3] = 0;
		result[3][4] = 1;
		result[3][5] = 1;
		result[4][0] = -1;
		result[4][1] = -1;
		result[4][2] = 1;
		result[4][3] = -1;
		result[4][4] = 0;
		result[4][5] = 1;
		return result;
	}
	public static Map<Integer, Map<Integer, List<UnifiedAlphabetSymbol>>> getRules() {
		Map<Integer, Map<Integer, List<UnifiedAlphabetSymbol>>> result = new HashMap<>();
		HashMap<Integer, List<UnifiedAlphabetSymbol>> productions = new HashMap<>();
		List<UnifiedAlphabetSymbol> production = new ArrayList<>();
		production.add(new UnifiedAlphabetSymbol(0, true));
		productions.put(0, production);
		production = new ArrayList<>();
		production.add(new UnifiedAlphabetSymbol(1, true));
		production.add(new UnifiedAlphabetSymbol(1, false));
		production.add(new UnifiedAlphabetSymbol(2, true));
		productions.put(1, production);
		production = new ArrayList<>();
		result.put(0, productions);
		productions = new HashMap<>();
		production.add(new UnifiedAlphabetSymbol(2, false));
		production.add(new UnifiedAlphabetSymbol(4, false));
		productions.put(0, production);
		production = new ArrayList<>();
		result.put(1, productions);
		productions = new HashMap<>();
		production.add(new UnifiedAlphabetSymbol(0, false));
		production.add(new UnifiedAlphabetSymbol(3, false));
		productions.put(0, production);
		production = new ArrayList<>();
		result.put(2, productions);
		productions = new HashMap<>();
		production.add(new UnifiedAlphabetSymbol(3, true));
		production.add(new UnifiedAlphabetSymbol(0, false));
		production.add(new UnifiedAlphabetSymbol(3, false));
		productions.put(0, production);
		production = new ArrayList<>();
		productions.put(1, production);
		production = new ArrayList<>();
		result.put(3, productions);
		productions = new HashMap<>();
		production.add(new UnifiedAlphabetSymbol(4, true));
		production.add(new UnifiedAlphabetSymbol(2, false));
		production.add(new UnifiedAlphabetSymbol(4, false));
		productions.put(0, production);
		production = new ArrayList<>();
		productions.put(1, production);
		production = new ArrayList<>();
		result.put(4, productions);
		productions = new HashMap<>();
		return result;
	}
}
