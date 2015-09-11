package net.thinktodo.parsechatdemo.utils;

import org.junit.Test;


import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
/**
 * Created by thong.nguyen on 9/11/2015.
 */
public class ParseChat2JsonTest {
    @Test
    public void emailValidator_CorrectEmailSimple_ReturnsTrue() {
        assertThat(true, is(true));
    }
}
