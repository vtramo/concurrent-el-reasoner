package saturation.rules;

import saturation.context.*;

public class ContextCompletionRuleFactory {

    private static final ContextCompletionRule<ContextCR1> CR1 = new ContextCompletionRuleCR1();
    private static final ContextCompletionRule<ContextCR2> CR2 = new ContextCompletionRuleCR2();
    private static final ContextCompletionRule<ContextCR3> CR3 = new ContextCompletionRuleCR3();
    private static final ContextCompletionRule<ContextCR4> CR4 = new ContextCompletionRuleCR4();
    private static final ContextCompletionRule<ContextCR5> CR5 = new ContextCompletionRuleCR5();
    private static final ContextCompletionRule<ContextCR6> CR6 = new ContextCompletionRuleCR6();

    public ContextCompletionRule<ContextCR1> buildContextContextCompletionRule(ContextCR1 context) {
        return CR1;
    }

    public ContextCompletionRule<ContextCR2> buildContextContextCompletionRule(ContextCR2 context) {
        return CR2;
    }

    public ContextCompletionRule<ContextCR3> buildContextContextCompletionRule(ContextCR3 context) {
        return CR3;
    }

    public ContextCompletionRule<ContextCR4> buildContextContextCompletionRule(ContextCR4 context) {
        return CR4;
    }

    public ContextCompletionRule<ContextCR5> buildContextContextCompletionRule(ContextCR5 context) {
        return CR5;
    }

    public ContextCompletionRule<ContextCR6> buildContextContextCompletionRule(ContextCR6 context) {
        return CR6;
    }
}
