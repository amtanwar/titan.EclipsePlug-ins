package org.eclipse.titan.designer.parsers.asn1parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.eclipse.core.resources.IFile;
import org.eclipse.titan.designer.AST.ASN1.BlockV4;

public class SyntaxLevelTokenStreamTrackerV4 extends BufferedTokenStream {
	private HashSet<Integer> discardMask = new HashSet<Integer>();
	private IFile sourceFile;
	private int index;
	private List<Token> oldList;

	protected SyntaxLevelTokenStreamTrackerV4(TokenSource blockV4, int startIndex) {
		super(blockV4);
		this.index = startIndex;
		this.oldList = ((BlockV4)blockV4).getTokenList();
	}
	public void setActualFile(IFile sourceFile) {
		this.sourceFile = sourceFile;
	}
	
	public void discard(int ttype) {
		discardMask.add(Integer.valueOf(ttype));
	}

	public int getActualIndex() {
		return index;
	}

	@Override
	public int fetch (int n) {
		if (fetchedEOF) {
			return 0;
		}	
		Token t;
		Token first;
		int i = 0;
		
		if (oldList == null || index >= oldList.size()) {
			tokens.add(new TokenWithIndexAndSubTokensV4(Token.EOF));
			return ++i;
		}
		
		do {
			t = oldList.get(index++);
			//t = tokens.get(index++);
			first = t;
			if (t == null) {
				return 0;
			} else if (discardMask.contains(Integer.valueOf(t.getType()))) {
				// discard this Token
			} else if (t.getType() == ASN1Lexer2.SQUAREOPEN) {
				boolean exit = getBlock(first);
				if (exit) return ++i;
				++i;
				--n;
			} else {
				tokens.add(t);
				++i;
				--n;
			}
		} while (0 < n);
		return i;
	}

	public static ASN1Parser2 getASN1ParserForBlock(BlockV4 blockV4) {
		return getASN1ParserForBlock(blockV4, 0);
	}
	
	public static ASN1Parser2 getASN1ParserForBlock(BlockV4 blockV4, int startIndex) {
		if(blockV4 == null || blockV4.getLocation() == null) {
			return null;
		}

		SyntaxLevelTokenStreamTrackerV4 tracker = new SyntaxLevelTokenStreamTrackerV4(blockV4, startIndex);
		tracker.discard(ASN1Lexer2.WS);
		tracker.discard(ASN1Lexer2.MULTILINECOMMENT);
		tracker.discard(ASN1Lexer2.SINGLELINECOMMENT);
		
		ASN1Parser2 parser = new ASN1Parser2(tracker);
		tracker.setActualFile((IFile) blockV4.getLocation().getFile());
		parser.setActualFile((IFile) blockV4.getLocation().getFile());
		parser.setBuildParseTree(false);
		ASN1Listener parserListener = new ASN1Listener(parser);
		parser.removeErrorListeners(); // remove ConsoleErrorListener
		parser.addErrorListener(parserListener);		
		return parser;
	}

	private boolean getBlock(Token first) {
		Token t;
		TokenWithIndexAndSubTokensV4 result;
		if(index >= oldList.size()) {
			tokens.add(first);
			return true;
		}
		t = oldList.get(index++);
		List<Token> tokenList = new ArrayList<Token>();
		int nofUnclosedParanthesis = 1;
		while(t != null && t.getType() != Token.EOF && index < oldList.size()) {
			if(t.getType() == ASN1Lexer2.SQUAREOPEN) {
				nofUnclosedParanthesis++;
			} else if(t.getType() == ASN1Lexer2.SQUARECLOSE) {
				nofUnclosedParanthesis--;
				if(nofUnclosedParanthesis == 0) {
					result = new TokenWithIndexAndSubTokensV4(ASN1Lexer2.BLOCK, tokenList, sourceFile);
					result.setCharPositionInLine(first.getCharPositionInLine());
					result.setLine(first.getLine());
					result.setStartIndex(((TokenWithIndexAndSubTokensV4) first).getStopIndex());
					result.setStopIndex(((TokenWithIndexAndSubTokensV4) t).getStopIndex());
					result.setText(makeString(tokenList));
					tokens.add(result);
					return false;
				}
			}
			if(!discardMask.contains(Integer.valueOf(t.getType()))) {
				tokenList.add(t);
			}
			t = oldList.get(index++);
		}
		result = new TokenWithIndexAndSubTokensV4(ASN1Lexer2.BLOCK, tokenList, sourceFile);
		result.setCharPositionInLine(first.getCharPositionInLine());
		result.setLine(first.getLine());
		result.setStartIndex(((TokenWithIndexAndSubTokensV4) first).getStopIndex());
		if (t != null) {
			result.setStopIndex(((TokenWithIndexAndSubTokensV4) t).getStopIndex());
		}
		tokens.add(result);
		return true;
	}

	private String makeString(List<Token> list) {
		StringBuilder text = new StringBuilder();
		for (Token t : list) {
			text.append(t.getText());
		}
		return text.toString();
	}
}
